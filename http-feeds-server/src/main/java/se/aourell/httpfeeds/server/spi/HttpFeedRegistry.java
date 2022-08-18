package se.aourell.httpfeeds.server.spi;

import se.aourell.httpfeeds.server.core.HttpFeedDefinition;

import java.util.Optional;

public interface HttpFeedRegistry {

  String validateFeedPath(String path);

  HttpFeedDefinition defineFeed(String path, FeedItemService feedItemService);

  Optional<HttpFeedDefinition> getDefinedFeed(String path);
}
