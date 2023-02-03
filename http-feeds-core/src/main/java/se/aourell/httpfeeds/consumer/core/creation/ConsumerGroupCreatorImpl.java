package se.aourell.httpfeeds.consumer.core.creation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.consumer.api.ConsumerCreator;
import se.aourell.httpfeeds.consumer.api.ConsumerGroupCreator;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessorGroup;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;
import se.aourell.httpfeeds.producer.core.EventFeedDefinition;

import java.util.function.Consumer;

public class ConsumerGroupCreatorImpl implements ConsumerGroupCreator, Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(ConsumerGroupCreatorImpl.class);
  private static final long INITIAL_DELAY_TIMEOUT_MS = 5_000;
  private static final long POLL_INTERMITTENT_DELAY_MS = 1_000;

  private final FeedConsumerProcessorGroup feedConsumerProcessorGroup;

  private int manuallyDefinedConsumerIndex = 0;

  public ConsumerGroupCreatorImpl(LocalFeedFetcher localFeedFetcher, RemoteFeedFetcher remoteFeedFetcher, DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository) {
    feedConsumerProcessorGroup = new FeedConsumerProcessorGroup(localFeedFetcher, remoteFeedFetcher, domainEventDeserializer, feedConsumerRepository);
  }

  @Override
  public ConsumerGroupCreator defineLocalConsumer(String feedName, Consumer<ConsumerCreator> consumer) {
    final String feedConsumerName = generateUniqueConsumerName(feedName);

    return defineLocalConsumer(feedName, feedConsumerName, consumer);
  }

  @Override
  public ConsumerGroupCreator defineLocalConsumer(String feedName, String feedConsumerName, Consumer<ConsumerCreator> consumer) {
    final FeedConsumerProcessor processor = feedConsumerProcessorGroup.defineLocalConsumer(feedConsumerName, feedName);
    final ConsumerCreator consumerCreator = new ConsumerCreatorImpl(processor);
    consumer.accept(consumerCreator);

    LOG.debug("Defined Event Consumer (Local) for feed '{}' with unique name '{}'", feedName, feedConsumerName);
    return this;
  }

  @Override
  public ConsumerGroupCreator defineRemoteConsumer(String feedName, String baseUri, Consumer<ConsumerCreator> consumer) {
    final String feedConsumerName = generateUniqueConsumerName(feedName);
    final String feedUrl = EventFeedDefinition.fullUrlFromBaseUriAndFeedName(baseUri, feedName);

    return defineRemoteConsumer(feedName, feedConsumerName, feedUrl, consumer);
  }

  @Override
  public ConsumerGroupCreator defineRemoteConsumer(String feedName, String feedConsumerName, String completeFeedUrl, Consumer<ConsumerCreator> consumer) {
    final FeedConsumerProcessor processor = feedConsumerProcessorGroup.defineRemoteConsumer(feedConsumerName, feedName, completeFeedUrl);
    final ConsumerCreator consumerCreator = new ConsumerCreatorImpl(processor);
    consumer.accept(consumerCreator);

    LOG.debug("Defined Event Consumer (Remote) for feed '{}' with unique name '{}' for URL '{}'", feedName, feedConsumerName, completeFeedUrl);
    return this;
  }

  private String generateUniqueConsumerName(String feedName) {
    ++manuallyDefinedConsumerIndex;
    return "feed-consumer:" + manuallyDefinedConsumerIndex + ":" + feedName;
  }

  @Override
  public void run() {
    try {
      Thread.sleep(INITIAL_DELAY_TIMEOUT_MS);
    } catch (InterruptedException e) {
      LOG.info("Interrupted, shutting down");
      return;
    }

    while (true) {
      feedConsumerProcessorGroup.batchFetchAndProcessEvents();

      try {
        Thread.sleep(POLL_INTERMITTENT_DELAY_MS);
      } catch (InterruptedException e) {
        LOG.info("Interrupted, shutting down");
        return;
      }
    }
  }
}
