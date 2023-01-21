package se.aourell.httpfeeds.producer.spi;

import se.aourell.httpfeeds.producer.core.EventFeedDefinition;

import java.util.Optional;

public interface EventFeedRegistry {

  EventFeedDefinition defineFeed(String name, FeedItemService feedItemService, boolean publishHttpFeed);

  Optional<EventFeedDefinition> getLocalFeedByName(String name);

  Optional<EventFeedDefinition> getPublishedHttpFeedByPath(String path);
}
