package se.aourell.httpfeeds.core;

import se.aourell.httpfeeds.api.HttpFeed;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpFeedRegistryImpl implements HttpFeedRegistry {

  private final Map<String, HttpFeedDefinition> feedDefinitions = new HashMap<>();

  @Override
  public void defineFeed(HttpFeed feed, FeedService feedService) {
    final var feedDefinition = new HttpFeedDefinition(feed.feed(), feed.path(), feed.table(), feedService);

    var path = feed.feed();
    while (path.startsWith("/")) {
      path = path.substring(1);
    }
    while (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    feedDefinitions.put(feed.feed(), feedDefinition);
  }

  @Override
  public Optional<HttpFeedDefinition> getDefinedFeed(String name) {
    final var feedDefinition = feedDefinitions.get(name);
    if (feedDefinition == null) {
      return Optional.empty();
    }
    return Optional.of(feedDefinition);
  }
}
