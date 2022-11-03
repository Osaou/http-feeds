package se.aourell.httpfeeds.producer.spi;

import se.aourell.httpfeeds.producer.core.EventFeedDefinition;

import java.util.Optional;

public interface EventFeedRegistry {

  EventFeedDefinition defineFeed(String path, FeedItemService feedItemService, boolean publishHttpFeed);

  Optional<EventFeedDefinition> getPublishedHttpFeed(String path);
}
