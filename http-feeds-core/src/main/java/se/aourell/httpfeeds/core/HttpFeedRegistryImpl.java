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
  public HttpFeedDefinition defineFeed(HttpFeed feed, FeedItemService feedItemService) {
    requireNonEmpty(feed.path());
    requireNonEmpty(feed.feedName());
    requireNonEmpty(feed.persistenceName());

    var path = feed.path();
    if (!path.startsWith(HttpFeedDefinition.PATH_PREFIX)) {
      throw new IllegalArgumentException("Feed path must start with \"" + HttpFeedDefinition.PATH_PREFIX + "\"");
    }
    while (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    if (path.isEmpty()) {
      throw new IllegalArgumentException("Feed path must be defined");
    }
    path = path + "/";

    final var feedDefinition = new HttpFeedDefinition(feed.feedName(), path, feed.persistenceName(), feedItemService);
    feedDefinitions.put(path, feedDefinition);

    return feedDefinition;
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

  private void requireNonEmpty(String value) {
    if (value == null || "".equals(value.trim())) {
      throw new IllegalArgumentException("Feed path must be defined");
    }
  }
}
