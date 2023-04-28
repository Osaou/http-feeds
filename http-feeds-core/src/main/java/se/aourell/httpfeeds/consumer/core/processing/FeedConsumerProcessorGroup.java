package se.aourell.httpfeeds.consumer.core.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;
import se.aourell.httpfeeds.tracing.core.DeadLetterQueueException;
import se.aourell.httpfeeds.tracing.core.DeadLetterQueueService;
import se.aourell.httpfeeds.tracing.spi.ApplicationShutdownDetector;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.util.Assert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FeedConsumerProcessorGroup {

  private static final Logger LOG = LoggerFactory.getLogger(FeedConsumerProcessorGroup.class);

  private static final long FAILURE_TIMEOUT_MS = 1_000;
  private static final long MAX_FAILURE_COUNT_FOR_EXPONENTIAL_BACKOFF_EFFECT = 5;

  private final LocalFeedFetcher localFeedFetcher;
  private final RemoteFeedFetcher remoteFeedFetcher;
  private final DomainEventDeserializer domainEventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;
  private final ApplicationShutdownDetector applicationShutdownDetector;
  private final DeadLetterQueueService deadLetterQueueService;
  private final DeadLetterQueueRepository deadLetterQueueRepository;
  private final String groupName;
  private final List<FeedConsumer> consumers = new ArrayList<>();

  private long failureCount;

  public FeedConsumerProcessorGroup(LocalFeedFetcher localFeedFetcher,
                                    RemoteFeedFetcher remoteFeedFetcher,
                                    DomainEventDeserializer domainEventDeserializer,
                                    FeedConsumerRepository feedConsumerRepository,
                                    ApplicationShutdownDetector applicationShutdownDetector,
                                    DeadLetterQueueService deadLetterQueueService,
                                    DeadLetterQueueRepository deadLetterQueueRepository,
                                    String groupName) {
    this.localFeedFetcher = Assert.notNull(localFeedFetcher);
    this.remoteFeedFetcher = Assert.notNull(remoteFeedFetcher);
    this.domainEventDeserializer = Assert.notNull(domainEventDeserializer);
    this.feedConsumerRepository = Assert.notNull(feedConsumerRepository);
    this.applicationShutdownDetector = Assert.notNull(applicationShutdownDetector);
    this.deadLetterQueueService = Assert.notNull(deadLetterQueueService);
    this.deadLetterQueueRepository = Assert.notNull(deadLetterQueueRepository);
    this.groupName = Assert.hasStringValue(groupName);

    this.failureCount = 0;
  }

  public FeedConsumer defineLocalConsumer(String feedConsumerName, String feedName) {
    Assert.notNull(feedConsumerName);
    Assert.notNull(feedName);

    return addConsumer(feedConsumerName, feedName, null);
  }

  public FeedConsumer defineRemoteConsumer(String feedConsumerName, String feedName, String feedUrl) {
    Assert.notNull(feedConsumerName);
    Assert.notNull(feedName);
    Assert.notNull(feedUrl);

    return addConsumer(feedConsumerName, feedName, feedUrl);
  }

  private FeedConsumer addConsumer(String feedConsumerName, String feedName, String feedUrl) {
    final var consumer = new FeedConsumer(feedConsumerName, feedName, feedUrl, localFeedFetcher, remoteFeedFetcher, domainEventDeserializer, feedConsumerRepository, applicationShutdownDetector, deadLetterQueueService, deadLetterQueueRepository);
    consumers.add(consumer);

    return consumer;
  }

  public void batchFetchAndProcessEvents() {
    final boolean hasPreviousFailure = failureCount > 0;
    if (hasPreviousFailure) {
      try {
        // employ exponential backoff strategy, with ceiling
        final long effect = (long) Math.pow(2, failureCount);
        final long backOffTimerMs = effect * FAILURE_TIMEOUT_MS;

        LOG.trace("Sleeping for {} ms because of earlier failure", backOffTimerMs);
        Thread.sleep(backOffTimerMs);
      } catch (InterruptedException e) {
        if (applicationShutdownDetector.isGracefulShutdown()) {
          return;
        }

        LOG.warn("Unexpectedly interrupted while sleeping because of earlier failure");
      }
    }

    // fetch events from all registered consumers
    List<FetchedEventFromConsumer> events = null;
    Throwable problem = null;
    long updatedFailureCount = 0;
    try {
      events = consumers.stream()
        .flatMap(consumer -> {
          if (applicationShutdownDetector.isGracefulShutdown()) {
            return Stream.empty();
          }

          return consumer.fetchEvents()
            .orElseThrow()
            .stream()
            .map(event -> new FetchedEventFromConsumer(consumer, event));
        })
        .toList();
    } catch (Throwable e) {
      if (applicationShutdownDetector.isGracefulShutdown()) {
        return;
      }

      updatedFailureCount = increaseFailureCount();
      problem = e;
    }

    final boolean hasEvents = events != null && !events.isEmpty();

    // sort and then process total stream of events
    if (hasEvents) {
      try {
        // if we are consuming more than one source, we need to ensure correct ordering amongst the event streams
        if (consumers.size() > 1) {
          events = events.stream()
            .sorted(Comparator.comparing(fetched -> fetched.event().id()))
            .toList();
        }

        events.stream()
          .filter(fetched -> !applicationShutdownDetector.isGracefulShutdown())
          .forEach(fetched -> fetched.consumer.processEvent(fetched.event())
            // reset failure count when we succeed in processing an event
            .ifSuccess(__ -> failureCount = 0)
            .orElseThrow()
          );
      } catch (DeadLetterQueueException e) {
        // neither increase nor decrease current failure count when processing DLQ
        updatedFailureCount = failureCount;
      } catch (Throwable e) {
        if (applicationShutdownDetector.isGracefulShutdown()) {
          return;
        }

        LOG.error("Exception when processing event", e);

        updatedFailureCount = increaseFailureCount();
        problem = e;
      }

      // persist all feed consumers' event consumption progress
      try {
        consumers.stream()
          .filter(fetched -> !applicationShutdownDetector.isGracefulShutdown())
          .forEach(FeedConsumer::persistProgress);
      } catch (Throwable e) {
        if (applicationShutdownDetector.isGracefulShutdown()) {
          return;
        }

        if (problem == null) {
          updatedFailureCount = increaseFailureCount();
          problem = e;
        }
      }
    }

    if (updatedFailureCount > 0 && !hasPreviousFailure) {
      if (problem != null) {
        // going into failure mode
        LOG.warn("Problem consuming events for Consumer Group " + groupName + ": " + problem.getMessage());
      }
    } else if (updatedFailureCount <= 0 && hasPreviousFailure) {
      // exiting failure mode
      LOG.warn("Successful resume for Consumer Group {}", groupName);
    }

    failureCount = updatedFailureCount;
  }

  private long increaseFailureCount() {
    return Math.min(failureCount + 1, MAX_FAILURE_COUNT_FOR_EXPONENTIAL_BACKOFF_EFFECT);
  }

  private record FetchedEventFromConsumer(FeedConsumer consumer, CloudEvent event) { }
}
