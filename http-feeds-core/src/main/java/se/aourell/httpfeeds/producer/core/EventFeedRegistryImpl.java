package se.aourell.httpfeeds.producer.core;

import se.aourell.httpfeeds.producer.spi.FeedItemService;
import se.aourell.httpfeeds.producer.spi.EventFeedRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EventFeedRegistryImpl implements EventFeedRegistry {

  private final Map<String, EventFeedDefinition> nameMap = new HashMap<>();
  private final Map<String, EventFeedDefinition> pathMap = new HashMap<>();

  @Override
  public EventFeedDefinition defineFeed(String name, FeedItemService feedItemService, boolean publishHttpFeed) {
    requireNonEmpty(name);

    final String path = EventFeedDefinition.urlPathFromFeedName(name);
    final var feedDefinition = new EventFeedDefinition(name, path, feedItemService, publishHttpFeed);
    nameMap.put(name, feedDefinition);
    pathMap.put(path, feedDefinition);

    return feedDefinition;
  }

  @Override
  public Optional<EventFeedDefinition> getLocalFeedByName(String name) {
    final EventFeedDefinition feedDefinition = nameMap.get(name);
    return Optional.ofNullable(feedDefinition);
  }

  @Override
  public Optional<EventFeedDefinition> getPublishedHttpFeedByPath(String path) {
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    final EventFeedDefinition feedDefinition = pathMap.get(path);
    if (feedDefinition == null || !feedDefinition.publishedHttpFeed()) {
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
