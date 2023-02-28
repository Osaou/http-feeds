package se.aourell.httpfeeds.producer.spi;

import java.util.Optional;

public interface EventFeedsRegistry {

  void defineFeed(EventFeedService eventFeedService);

  Optional<EventFeedService> getLocalFeedByName(String name);

  Optional<EventFeedService> getPublishedHttpFeedByPath(String path);
}
