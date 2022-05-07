package se.aourell.httpfeeds.spi;

import se.aourell.httpfeeds.api.HttpFeed;
import se.aourell.httpfeeds.core.HttpFeedDefinition;

import java.util.Optional;

public interface HttpFeedRegistry {

  void defineFeed(HttpFeed feed, FeedItemService feedItemService);

  Optional<HttpFeedDefinition> getDefinedFeed(String name);
}
