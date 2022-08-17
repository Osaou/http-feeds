package se.aourell.httpfeeds.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.client.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.client.spi.FeedConsumerProcessor;
import se.aourell.httpfeeds.client.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.client.spi.HttpFeedsClient;
import se.aourell.httpfeeds.core.util.Result;

import java.util.ArrayList;
import java.util.List;

public class FeedConsumerProcessorImpl implements FeedConsumerProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(FeedConsumerProcessorImpl.class);

  private static final long FAILURE_TIMEOUT_MS = 2_000;
  private static final long MAX_FAILURE_COUNT = 5;

  private final HttpFeedsClient httpFeedsClient;
  private final DomainEventDeserializer domainEventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;
  private final List<FeedConsumerDefinition> consumerDefinitions = new ArrayList<>();

  private long failureCount = 0;

  public FeedConsumerProcessorImpl(HttpFeedsClient httpFeedsClient, DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository) {
    this.httpFeedsClient = httpFeedsClient;
    this.domainEventDeserializer = domainEventDeserializer;
    this.feedConsumerRepository = feedConsumerRepository;
  }

  @Override
  public FeedConsumerDefinition defineConsumer(String feedName, String url, Object bean) {
    final var packageName = bean.getClass().getPackageName();
    final var lastProcessedId = feedConsumerRepository.retrieveLastProcessedId(feedName).orElse(null);
    final var consumerDefinition = new FeedConsumerDefinition(feedName, url, bean, packageName, domainEventDeserializer, lastProcessedId);
    consumerDefinitions.add(consumerDefinition);

    return consumerDefinition;
  }

  @Override
  public void batchPollAndProcessEvents() {
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

    for (final var consumerDefinition : consumerDefinitions) {
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
            LOG.error("Exception when processing event", e);
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
