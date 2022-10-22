package se.aourell.httpfeeds.publisher.core;

import se.aourell.httpfeeds.publisher.spi.FeedItemService;
import se.aourell.httpfeeds.publisher.spi.EventFeedRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EventFeedRegistryImpl implements EventFeedRegistry {

  private final Map<String, EventFeedDefinition> feedDefinitions = new HashMap<>();

  @Override
  public EventFeedDefinition defineFeed(String name, FeedItemService feedItemService, boolean publishHttpFeed) {
    requireNonEmpty(name);

    final var feedPath = EventFeedDefinition.feedPathFromName(name);
    final var feedDefinition = new EventFeedDefinition(name, feedPath, feedItemService, publishHttpFeed);
    feedDefinitions.put(feedPath, feedDefinition);

    return feedDefinition;
  }

  @Override
  public Optional<EventFeedDefinition> getPublishedHttpFeed(String path) {
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    final var feedDefinition = feedDefinitions.get(path);
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
