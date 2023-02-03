package se.aourell.httpfeeds.consumer.core.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FeedConsumerProcessorGroup {

  private static final Logger LOG = LoggerFactory.getLogger(FeedConsumerProcessorGroup.class);

  private static final long FAILURE_TIMEOUT_MS = 2_000;
  private static final long MAX_FAILURE_COUNT = 5;

  private final LocalFeedFetcher localFeedFetcher;
  private final RemoteFeedFetcher remoteFeedFetcher;
  private final DomainEventDeserializer domainEventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;
  private final List<FeedConsumerProcessor> processors = new ArrayList<>();

  private long failureCount = 0;

  public FeedConsumerProcessorGroup(LocalFeedFetcher localFeedFetcher, RemoteFeedFetcher remoteFeedFetcher, DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository) {
    this.localFeedFetcher = localFeedFetcher;
    this.remoteFeedFetcher = remoteFeedFetcher;
    this.domainEventDeserializer = domainEventDeserializer;
    this.feedConsumerRepository = feedConsumerRepository;
  }

  public FeedConsumerProcessor defineLocalConsumer(String feedConsumerName, String feedName) {
    return addProcessor(feedConsumerName, feedName, null);
  }

  public FeedConsumerProcessor defineRemoteConsumer(String feedConsumerName, String feedName, String feedUrl) {
    Objects.requireNonNull(feedUrl);
    return addProcessor(feedConsumerName, feedName, feedUrl);
  }

  private FeedConsumerProcessor addProcessor(String feedConsumerName, String feedName, String feedUrl) {
    Objects.requireNonNull(feedConsumerName);
    Objects.requireNonNull(feedName);
    final var processor = new FeedConsumerProcessor(feedConsumerName, feedName, feedUrl, localFeedFetcher, remoteFeedFetcher, domainEventDeserializer, feedConsumerRepository);

    processors.add(processor);
    return processor;
  }

  public void batchFetchAndProcessEvents() {
    if (failureCount > 0) {
      try {
        // employ exponential backoff strategy, with ceiling
        final long effect = (long) Math.pow(2, failureCount);
        final long backOffTimerMs = effect * FAILURE_TIMEOUT_MS;

        LOG.trace("Sleeping for {} ms because of earlier failure", backOffTimerMs);
        Thread.sleep(backOffTimerMs);
      } catch (InterruptedException e) {
        LOG.info("Interrupted while sleeping because of earlier failure");
        return;
      }
    }

    for (final var processor : processors) {
      final long updatedFailureCount = processor.fetchAndProcessEvents()
        .orElseGet(() -> Math.min(failureCount + 1, MAX_FAILURE_COUNT));

      if (updatedFailureCount > 0 && failureCount <= 0) {
        // going into failure mode
        LOG.warn("Problem consuming events from feed {}", processor.getFeedName());
      } else if (updatedFailureCount <= 0 && failureCount > 0) {
        // exiting failure mode
        LOG.warn("Successful resume from feed {}", processor.getFeedName());
      }

      failureCount = updatedFailureCount;
    }
  }
}
