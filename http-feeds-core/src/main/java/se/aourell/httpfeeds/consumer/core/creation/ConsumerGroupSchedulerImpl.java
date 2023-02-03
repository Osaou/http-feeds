package se.aourell.httpfeeds.consumer.core.creation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.consumer.api.ConsumerGroupCreator;
import se.aourell.httpfeeds.consumer.api.ConsumerGroupScheduler;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;

import java.util.function.Consumer;

public class ConsumerGroupSchedulerImpl implements ConsumerGroupScheduler {

  private static final Logger LOG = LoggerFactory.getLogger(ConsumerGroupSchedulerImpl.class);

  private final LocalFeedFetcher localFeedFetcher;
  private final RemoteFeedFetcher remoteFeedFetcher;
  private final DomainEventDeserializer domainEventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;

  public ConsumerGroupSchedulerImpl(LocalFeedFetcher localFeedFetcher, RemoteFeedFetcher remoteFeedFetcher, DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository) {
    this.localFeedFetcher = localFeedFetcher;
    this.remoteFeedFetcher = remoteFeedFetcher;
    this.domainEventDeserializer = domainEventDeserializer;
    this.feedConsumerRepository = feedConsumerRepository;
  }

  @Override
  public ConsumerGroupScheduler scheduleGroup(String groupName, Consumer<ConsumerGroupCreator> consumerGroup) {
    final var group = new ConsumerGroupCreatorImpl(localFeedFetcher, remoteFeedFetcher, domainEventDeserializer, feedConsumerRepository);
    consumerGroup.accept(group);

    final var thread = new Thread(group, groupName);
    LOG.debug("Scheduled new Consumer Group with thread name {}", groupName);

    thread.start();
    return this;
  }
}
