package se.aourell.httpfeeds.core;

import se.aourell.httpfeeds.api.HttpFeed;
import se.aourell.httpfeeds.spi.FeedItemService;
import se.aourell.httpfeeds.spi.HttpFeedRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpFeedRegistryImpl implements HttpFeedRegistry {

  private final Map<String, HttpFeedDefinition> feedDefinitions = new HashMap<>();

  @Override
  public void defineFeed(HttpFeed feed, FeedItemService feedItemService) {
    var path = feed.path();
    if (!path.startsWith(HttpFeedDefinition.pathPrefix)) {
      throw new IllegalArgumentException("Feed path must start with \"" + HttpFeedDefinition.pathPrefix + "\"");
    }
    while (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    if (path.isEmpty()) {
      throw new IllegalArgumentException("Feed path must be defined");
    }

    final var feedDefinition = new HttpFeedDefinition(feed.feedName(), feed.path(), feed.persistenceName(), feedItemService);
    feedDefinitions.put(path + "/", feedDefinition);
  }

  @Override
  public Optional<HttpFeedDefinition> getDefinedFeed(String path) {
    if (!path.endsWith("/")) {
      path = path + "/";
    }

    final var feedDefinition = feedDefinitions.get(path);
    if (feedDefinition == null) {
      return Optional.empty();
    }
    return Optional.of(feedDefinition);
  }
}
