package se.aourell.httpfeeds.consumer.core.creation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.consumer.api.ConsumerGroupCreator;
import se.aourell.httpfeeds.consumer.api.EventFeedsConsumerApi;
import se.aourell.httpfeeds.spi.ApplicationShutdownDetector;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;

import java.util.function.Consumer;

public class EventFeedsConsumerApiImpl implements EventFeedsConsumerApi {

  private static final Logger LOG = LoggerFactory.getLogger(EventFeedsConsumerApiImpl.class);

  private final ApplicationShutdownDetector applicationShutdownDetector;
  private final LocalFeedFetcher localFeedFetcher;
  private final RemoteFeedFetcher remoteFeedFetcher;
  private final DomainEventDeserializer domainEventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;

  public EventFeedsConsumerApiImpl(ApplicationShutdownDetector applicationShutdownDetector,
                                   LocalFeedFetcher localFeedFetcher,
                                   RemoteFeedFetcher remoteFeedFetcher,
                                   DomainEventDeserializer domainEventDeserializer,
                                   FeedConsumerRepository feedConsumerRepository) {
    this.applicationShutdownDetector = applicationShutdownDetector;
    this.localFeedFetcher = localFeedFetcher;
    this.remoteFeedFetcher = remoteFeedFetcher;
    this.domainEventDeserializer = domainEventDeserializer;
    this.feedConsumerRepository = feedConsumerRepository;
  }

  @Override
  public EventFeedsConsumerApi scheduleConsumerGroup(String groupName, Consumer<ConsumerGroupCreator> consumerGroup) {
    final var group = new ConsumerGroupCreatorImpl(applicationShutdownDetector, localFeedFetcher, remoteFeedFetcher, domainEventDeserializer, feedConsumerRepository, groupName);

    LOG.debug("Scheduling new Consumer Group with thread name {}", groupName);
    consumerGroup.accept(group);

    final var thread = new Thread(group, groupName);
    thread.start();

    return this;
  }
}
