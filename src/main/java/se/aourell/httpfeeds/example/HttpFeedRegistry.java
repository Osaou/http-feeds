package se.aourell.httpfeeds.example;

import java.util.Optional;

public interface HttpFeedRegistry {

  void defineFeed(HttpFeed feed, FeedFetcher feedFetcher);

  Optional<HttpFeedDefinition> getDefinedFeed(String name);
}
