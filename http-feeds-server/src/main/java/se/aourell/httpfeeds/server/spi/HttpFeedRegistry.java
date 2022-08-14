package se.aourell.httpfeeds.server.spi;

import se.aourell.httpfeeds.server.api.HttpFeed;
import se.aourell.httpfeeds.server.core.HttpFeedDefinition;

import java.util.Optional;

public interface HttpFeedRegistry {

  HttpFeedDefinition defineFeed(HttpFeed feed, FeedItemService feedItemService);

  Optional<HttpFeedDefinition> getDefinedFeed(String path);
}
