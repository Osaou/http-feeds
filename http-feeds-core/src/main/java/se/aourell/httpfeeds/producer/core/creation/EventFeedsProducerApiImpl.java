package se.aourell.httpfeeds.producer.core.creation;

import se.aourell.httpfeeds.producer.api.EventFeedsProducerApi;
import se.aourell.httpfeeds.producer.api.FeedAvailability;
import se.aourell.httpfeeds.producer.core.EventBusImpl;
import se.aourell.httpfeeds.producer.core.EventFeedServiceImpl;
import se.aourell.httpfeeds.producer.core.EventFeedsUtil;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.producer.spi.EventBus;
import se.aourell.httpfeeds.producer.spi.EventFeedService;
import se.aourell.httpfeeds.producer.spi.EventFeedsRegistry;
import se.aourell.httpfeeds.producer.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;
import se.aourell.httpfeeds.producer.spi.FeedItemRepositoryFactory;
import se.aourell.httpfeeds.spi.ApplicationShutdownDetector;

import java.time.Duration;

public class EventFeedsProducerApiImpl implements EventFeedsProducerApi {

  private final ApplicationShutdownDetector applicationShutdownDetector;
  private final FeedItemRepositoryFactory feedItemRepositoryFactory;
  private final FeedItemIdGenerator feedItemIdGenerator;
  private final DomainEventSerializer domainEventSerializer;
  private final EventFeedsRegistry eventFeedsRegistry;

  public EventFeedsProducerApiImpl(ApplicationShutdownDetector applicationShutdownDetector,
                                   FeedItemRepositoryFactory feedItemRepositoryFactory,
                                   FeedItemIdGenerator feedItemIdGenerator,
                                   DomainEventSerializer domainEventSerializer,
                                   EventFeedsRegistry eventFeedsRegistry) {
    this.applicationShutdownDetector = applicationShutdownDetector;
    this.feedItemRepositoryFactory = feedItemRepositoryFactory;
    this.feedItemIdGenerator = feedItemIdGenerator;
    this.domainEventSerializer = domainEventSerializer;
    this.eventFeedsRegistry = eventFeedsRegistry;
  }

  @Override
  public <EventBaseType> EventBus<EventBaseType> publishEventFeed(String feedName, Class<EventBaseType> feedEventBaseType, FeedAvailability availability, String persistenceName) {
    final boolean shouldPublish = switch (availability) {
      case PUBLISH_OVER_HTTP -> true;
      case APPLICATION_INTERNAL -> false;
    };
    final Duration pollInterval = EventFeedsUtil.DEFAULT_POLL_INTERVAL;
    final int limit = EventFeedsUtil.DEFAULT_LIMIT_COUNT_PER_REQUEST;

    final FeedItemRepository feedItemRepository = feedItemRepositoryFactory.apply(persistenceName, feedName);
    final EventFeedService eventFeedService = new EventFeedServiceImpl(applicationShutdownDetector, feedItemRepository, feedName, shouldPublish, pollInterval, limit);
    eventFeedsRegistry.defineFeed(eventFeedService);

    return new EventBusImpl<>(feedName, feedEventBaseType, feedItemRepository, feedItemIdGenerator, domainEventSerializer);
  }
}
