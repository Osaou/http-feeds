package se.aourell.httpfeeds.producer.core;

import se.aourell.httpfeeds.producer.spi.EventFeedService;
import se.aourell.httpfeeds.producer.spi.EventFeedsRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EventFeedsRegistryImpl implements EventFeedsRegistry {

  private final Map<String, EventFeedService> nameMap = new HashMap<>();
  private final Map<String, EventFeedService> pathMap = new HashMap<>();

  @Override
  public void defineFeed(EventFeedService eventFeedService) {
    final String name = eventFeedService.getName();
    nameMap.put(name, eventFeedService);

    final String path = eventFeedService.getFeedPath();
    pathMap.put(path, eventFeedService);
  }

  @Override
  public Optional<EventFeedService> getLocalFeedByName(String name) {
    final EventFeedService feedDefinition = nameMap.get(name);
    return Optional.ofNullable(feedDefinition);
  }

  @Override
  public Optional<EventFeedService> getPublishedHttpFeedByPath(String path) {
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    final EventFeedService feedDefinition = pathMap.get(path);
    if (feedDefinition == null || !feedDefinition.isPublishedOverHttp()) {
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
