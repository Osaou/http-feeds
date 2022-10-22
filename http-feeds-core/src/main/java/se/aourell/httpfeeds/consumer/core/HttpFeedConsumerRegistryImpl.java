package se.aourell.httpfeeds.consumer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.consumer.core.processing.EventFeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.core.processing.HttpFeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.HttpFeedConsumerRegistry;
import se.aourell.httpfeeds.consumer.spi.HttpFeedsClient;
import se.aourell.httpfeeds.publisher.core.EventFeedDefinition;
import se.aourell.httpfeeds.util.Result;

import java.util.ArrayList;
import java.util.List;

public class HttpFeedConsumerRegistryImpl implements HttpFeedConsumerRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(HttpFeedConsumerRegistryImpl.class);

  private static final long FAILURE_TIMEOUT_MS = 2_000;
  private static final long MAX_FAILURE_COUNT = 5;

  private final HttpFeedsClient httpFeedsClient;
  private final DomainEventDeserializer domainEventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;
  private final List<HttpFeedConsumerProcessor> httpFeedConsumerProcessors = new ArrayList<>();

  private long failureCount = 0;

  public HttpFeedConsumerRegistryImpl(HttpFeedsClient httpFeedsClient, DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository) {
    this.httpFeedsClient = httpFeedsClient;
    this.domainEventDeserializer = domainEventDeserializer;
    this.feedConsumerRepository = feedConsumerRepository;
  }

  @Override
  public EventFeedConsumerProcessor defineHttpFeedConsumer(String feedName, String baseUri, Object bean) {
    final var feedUrl = EventFeedDefinition.feedUrlFromName(baseUri, feedName);
    final var lastProcessedId = feedConsumerRepository.retrieveLastProcessedId(feedName).orElse(null);
    final var httpFeedConsumerDefinition = new HttpFeedConsumerProcessor(feedName, bean, feedUrl, domainEventDeserializer, lastProcessedId);

    httpFeedConsumerProcessors.add(httpFeedConsumerDefinition);
    return httpFeedConsumerDefinition;
  }

  @Override
  public void batchPollAndProcessHttpFeedEvents() {
    if (failureCount > 0) {
      try {
        // employ exponential backoff strategy, with ceiling
        final long effect = (long) Math.pow(2, failureCount);
        final long backOffTimerMs = effect * FAILURE_TIMEOUT_MS;

        LOG.info("Sleeping for {} ms because of earlier failure", backOffTimerMs);
        Thread.sleep(backOffTimerMs);
      } catch (InterruptedException e) {
        LOG.error("Interrupted while sleeping because of earlier failure", e);
      }
    }

    for (final var consumerDefinition : httpFeedConsumerProcessors) {
      final var url = consumerDefinition.getUrl();
      final var fetchedEvents = consumerDefinition.getLastProcessedId()
        .map(lastProcessedId -> httpFeedsClient.pollCloudEvents(url, lastProcessedId))
        .orElseGet(() -> httpFeedsClient.pollCloudEvents(url));

      final long updatedFailureCount = fetchedEvents
        .flatMap(events -> {
          try {
            for (final var event : events) {
              consumerDefinition.processEvent(event);
            }
          } catch (Exception e) {
            LOG.error("Exception when processing remote event", e);
            return Result.failure(e);
          }

          // reset failure count
          return Result.success(0L);
        })
        .orElseGet(() -> Math.min(failureCount + 1, MAX_FAILURE_COUNT));

      // persist id of last event we were able to process
      consumerDefinition.getLastProcessedId()
        .ifPresent(lastProcessedId -> {
          final var feedName = consumerDefinition.getFeedName();
          feedConsumerRepository.storeLastProcessedId(feedName, lastProcessedId);
        });

      failureCount = updatedFailureCount;
    }
  }
}
