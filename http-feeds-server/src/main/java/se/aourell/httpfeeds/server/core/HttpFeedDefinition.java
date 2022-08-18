package se.aourell.httpfeeds.server.core;

import se.aourell.httpfeeds.server.spi.FeedItemService;

public record HttpFeedDefinition(String path, FeedItemService feedItemService) {

  public static final String PATH_PREFIX = "/feed/";
}
