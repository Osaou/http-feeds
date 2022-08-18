package se.aourell.httpfeeds.server.core;

import se.aourell.httpfeeds.server.spi.FeedItemService;
import se.aourell.httpfeeds.server.spi.HttpFeedRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpFeedRegistryImpl implements HttpFeedRegistry {

  private final Map<String, HttpFeedDefinition> feedDefinitions = new HashMap<>();

  @Override
  public String validateFeedPath(String path) {
    requireNonEmpty(path);

    if (!path.startsWith(HttpFeedDefinition.PATH_PREFIX)) {
      throw new IllegalArgumentException("Feed path must start with \"" + HttpFeedDefinition.PATH_PREFIX + "\"");
    }
    while (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    if (path.isEmpty()) {
      throw new IllegalArgumentException("Feed path must be defined");
    }

    return path + "/";
  }

  @Override
  public HttpFeedDefinition defineFeed(String path, FeedItemService feedItemService) {
    requireNonEmpty(path);

    final var feedDefinition = new HttpFeedDefinition(path, feedItemService);
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
      throw new IllegalArgumentException("Feed values must be defined");
    }
  }
}
