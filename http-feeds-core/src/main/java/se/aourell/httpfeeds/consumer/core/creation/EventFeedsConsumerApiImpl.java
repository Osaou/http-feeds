package se.aourell.httpfeeds.consumer.core.creation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.consumer.api.ConsumerGroupCreator;
import se.aourell.httpfeeds.consumer.api.EventFeedsConsumerApi;
import se.aourell.httpfeeds.tracing.core.DeadLetterQueueService;
import se.aourell.httpfeeds.tracing.spi.ApplicationShutdownDetector;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.TransactionContext;

import java.util.function.Consumer;

public class EventFeedsConsumerApiImpl implements EventFeedsConsumerApi {

  private static final Logger LOG = LoggerFactory.getLogger(EventFeedsConsumerApiImpl.class);

  private final ApplicationShutdownDetector applicationShutdownDetector;
  private final TransactionContext transactionContext;
  private final LocalFeedFetcher localFeedFetcher;
  private final RemoteFeedFetcher remoteFeedFetcher;
  private final DomainEventDeserializer domainEventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;
  private final DeadLetterQueueService deadLetterQueueService;
  private final DeadLetterQueueRepository deadLetterQueueRepository;

  public EventFeedsConsumerApiImpl(ApplicationShutdownDetector applicationShutdownDetector,
                                   TransactionContext transactionContext,
                                   LocalFeedFetcher localFeedFetcher,
                                   RemoteFeedFetcher remoteFeedFetcher,
                                   DomainEventDeserializer domainEventDeserializer,
                                   FeedConsumerRepository feedConsumerRepository,
                                   DeadLetterQueueService deadLetterQueueService,
                                   DeadLetterQueueRepository deadLetterQueueRepository) {
    this.applicationShutdownDetector = applicationShutdownDetector;
    this.transactionContext = transactionContext;
    this.localFeedFetcher = localFeedFetcher;
    this.remoteFeedFetcher = remoteFeedFetcher;
    this.domainEventDeserializer = domainEventDeserializer;
    this.feedConsumerRepository = feedConsumerRepository;
    this.deadLetterQueueService = deadLetterQueueService;
    this.deadLetterQueueRepository = deadLetterQueueRepository;
  }

  @Override
  public EventFeedsConsumerApi scheduleConsumerGroup(String groupName, Consumer<ConsumerGroupCreator> consumerGroup) {
    final var group = new ConsumerGroupCreatorImpl(applicationShutdownDetector, transactionContext, localFeedFetcher, remoteFeedFetcher, domainEventDeserializer, feedConsumerRepository, deadLetterQueueService, deadLetterQueueRepository, groupName);

    LOG.debug("Scheduling new Consumer Group with thread name {}", groupName);
    consumerGroup.accept(group);

    final var thread = new Thread(group, groupName);
    thread.start();

    return this;
  }
}
