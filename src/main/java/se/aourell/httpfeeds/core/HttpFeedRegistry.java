package se.aourell.httpfeeds.core;

import se.aourell.httpfeeds.api.HttpFeed;

import java.util.Optional;

public interface HttpFeedRegistry {

  void defineFeed(HttpFeed feed, FeedService feedService);

  Optional<HttpFeedDefinition> getDefinedFeed(String name);
}
