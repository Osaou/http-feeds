package se.aourell.httpfeeds.consumer.core.creation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.consumer.api.ConsumerCreator;
import se.aourell.httpfeeds.consumer.api.ConsumerGroupCreator;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumer;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessorGroup;
import se.aourell.httpfeeds.tracing.core.DeadLetterQueueService;
import se.aourell.httpfeeds.tracing.spi.ApplicationShutdownDetector;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;
import se.aourell.httpfeeds.producer.core.EventFeedsUtil;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.TransactionContext;
import se.aourell.httpfeeds.util.Assert;

import java.util.function.Consumer;

public class ConsumerGroupCreatorImpl implements ConsumerGroupCreator, Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(ConsumerGroupCreatorImpl.class);
  private static final long INITIAL_DELAY_TIMEOUT_MS = 3_000;
  private static final long POLL_INTERMITTENT_DELAY_MS = 500;

  private final ApplicationShutdownDetector applicationShutdownDetector;
  private final TransactionContext transactionContext;
  private final FeedConsumerProcessorGroup feedConsumerProcessorGroup;

  private int manuallyDefinedConsumerIndex = 0;

  public ConsumerGroupCreatorImpl(ApplicationShutdownDetector applicationShutdownDetector,
                                  TransactionContext transactionContext,
                                  LocalFeedFetcher localFeedFetcher,
                                  RemoteFeedFetcher remoteFeedFetcher,
                                  DomainEventDeserializer domainEventDeserializer,
                                  FeedConsumerRepository feedConsumerRepository,
                                  DeadLetterQueueService deadLetterQueueService,
                                  DeadLetterQueueRepository deadLetterQueueRepository,
                                  String groupName) {
    this.applicationShutdownDetector = Assert.notNull(applicationShutdownDetector);
    this.transactionContext = Assert.notNull(transactionContext);
    this.feedConsumerProcessorGroup = new FeedConsumerProcessorGroup(localFeedFetcher, remoteFeedFetcher, domainEventDeserializer, feedConsumerRepository, applicationShutdownDetector, deadLetterQueueService, deadLetterQueueRepository, groupName);
  }

  @Override
  public ConsumerGroupCreator defineLocalConsumer(String feedName, Consumer<ConsumerCreator> consumer) {
    Assert.hasStringValue(feedName);
    Assert.notNull(consumer);

    final String feedConsumerName = generateUniqueConsumerName(feedName);

    return defineLocalConsumer(feedName, feedConsumerName, consumer);
  }

  @Override
  public ConsumerGroupCreator defineLocalConsumer(String feedName, String feedConsumerName, Consumer<ConsumerCreator> consumer) {
    Assert.hasStringValue(feedName);
    Assert.hasStringValue(feedConsumerName);
    Assert.notNull(consumer);

    final FeedConsumer processor = feedConsumerProcessorGroup.defineLocalConsumer(feedConsumerName, feedName);
    final ConsumerCreator consumerCreator = new ConsumerCreatorImpl(processor);

    LOG.debug("Defining Event Consumer (Local) for feed '{}' with unique name '{}'", feedName, feedConsumerName);
    consumer.accept(consumerCreator);

    return this;
  }

  @Override
  public ConsumerGroupCreator defineRemoteConsumer(String feedName, String baseUri, Consumer<ConsumerCreator> consumer) {
    Assert.hasStringValue(feedName);
    Assert.hasStringValue(baseUri);
    Assert.notNull(consumer);

    final String feedConsumerName = generateUniqueConsumerName(feedName);
    final String feedUrl = EventFeedsUtil.fullUrlFromBaseUriAndFeedName(baseUri, feedName);

    return defineRemoteConsumer(feedName, feedConsumerName, feedUrl, consumer);
  }

  @Override
  public ConsumerGroupCreator defineRemoteConsumer(String feedName, String feedConsumerName, String completeFeedUrl, Consumer<ConsumerCreator> consumer) {
    Assert.hasStringValue(feedName);
    Assert.hasStringValue(feedConsumerName);
    Assert.hasStringValue(completeFeedUrl);
    Assert.notNull(consumer);

    final FeedConsumer processor = feedConsumerProcessorGroup.defineRemoteConsumer(feedConsumerName, feedName, completeFeedUrl);
    final ConsumerCreator consumerCreator = new ConsumerCreatorImpl(processor);

    LOG.debug("Defined Event Consumer (Remote) for feed '{}' with unique name '{}' for URL '{}'", feedName, feedConsumerName, completeFeedUrl);
    consumer.accept(consumerCreator);

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
      if (applicationShutdownDetector.isGracefulShutdown()) {
        return;
      }

      LOG.warn("Unexpectedly interrupted while sleeping");
    }

    while (true) {
      try {
        transactionContext.executeInNewTransaction(feedConsumerProcessorGroup::batchFetchAndProcessEvents);
      } catch (Throwable e) {
        if (applicationShutdownDetector.isGracefulShutdown()) {
          return;
        }

        LOG.error("Unexpected error occurred during event processing", e);
      }

      try {
        Thread.sleep(POLL_INTERMITTENT_DELAY_MS);
      } catch (InterruptedException e) {
        if (applicationShutdownDetector.isGracefulShutdown()) {
          return;
        }

        LOG.warn("Unexpectedly interrupted while sleeping");
      }
    }
  }
}
