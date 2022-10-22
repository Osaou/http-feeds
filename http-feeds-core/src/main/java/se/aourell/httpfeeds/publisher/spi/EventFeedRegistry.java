package se.aourell.httpfeeds.publisher.spi;

import se.aourell.httpfeeds.publisher.core.EventFeedDefinition;

import java.util.Optional;

public interface EventFeedRegistry {

  EventFeedDefinition defineFeed(String path, FeedItemService feedItemService, boolean publishHttpFeed);

  Optional<EventFeedDefinition> getPublishedHttpFeed(String path);
}
