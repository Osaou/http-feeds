package se.aourell.httpfeeds.core;

import se.aourell.httpfeeds.spi.FeedItemService;

public record HttpFeedDefinition(String feed, String path, String table, FeedItemService feedItemService) {

  public static final String pathPrefix = "/feed/";
}
