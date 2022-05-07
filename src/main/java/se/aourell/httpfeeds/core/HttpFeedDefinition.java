package se.aourell.httpfeeds.core;

public record HttpFeedDefinition(
  String feed,
  String path,
  String table,
  FeedService feedService
) { }
