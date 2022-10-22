package se.aourell.httpfeeds.publisher.core;

import se.aourell.httpfeeds.publisher.spi.FeedItemService;

public record EventFeedDefinition(String name, String feedPath, FeedItemService feedItemService, boolean publishedHttpFeed) {

  public static final String PATH_PREFIX = "/feed/";

  public static String validateFeedName(String name) {
    if (name == null || "".equalsIgnoreCase(name.trim())) {
      throw new IllegalArgumentException("Feed feedPath must not be empty");
    }

    return name;
  }

  public static String feedUrlFromName(String baseUri, String name) {
    return baseUri + feedPathFromName(name);
  }

  public static String feedPathFromName(String name) {
    return EventFeedDefinition.PATH_PREFIX + name;
  }
}
