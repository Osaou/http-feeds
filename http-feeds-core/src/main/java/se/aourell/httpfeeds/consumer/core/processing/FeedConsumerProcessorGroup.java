package se.aourell.httpfeeds.consumer.core.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.producer.core.EventFeedDefinition;
import se.aourell.httpfeeds.util.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FeedConsumerProcessorGroup {

  private static final Logger LOG = LoggerFactory.getLogger(FeedConsumerProcessorGroup.class);

  private static final long FAILURE_TIMEOUT_MS = 2_000;
  private static final long MAX_FAILURE_COUNT = 5;

  private final DomainEventDeserializer domainEventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;
  private final List<FeedConsumerProcessor> processors = new ArrayList<>();

  private long failureCount = 0;

  public FeedConsumerProcessorGroup(DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository) {
    this.domainEventDeserializer = domainEventDeserializer;
    this.feedConsumerRepository = feedConsumerRepository;
  }

  public FeedConsumerProcessor defineFeedConsumer(String feedConsumerName, Object bean, String feedName, String feedUrl) {
    final var processor = new FeedConsumerProcessor(feedConsumerName, bean, feedName, feedUrl, domainEventDeserializer, feedConsumerRepository);

    processors.add(processor);
    return processor;
  }

  public void batchFetchAndProcessEvents(Function<FeedConsumerProcessor, Result<List<CloudEvent>>> fetch) {
    if (failureCount > 0) {
      try {
        // employ exponential backoff strategy, with ceiling
        final long effect = (long) Math.pow(2, failureCount);
        final long backOffTimerMs = effect * FAILURE_TIMEOUT_MS;

        LOG.trace("Sleeping for {} ms because of earlier failure", backOffTimerMs);
        Thread.sleep(backOffTimerMs);
      } catch (InterruptedException e) {
        LOG.error("Interrupted while sleeping because of earlier failure", e);
      }
    }

    for (final var processor : processors) {
      final long updatedFailureCount = processor.fetchAndProcessEvents(fetch)
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
