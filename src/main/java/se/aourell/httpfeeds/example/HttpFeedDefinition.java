package se.aourell.httpfeeds.example;

public record HttpFeedDefinition(
  String feed,
  String path,
  String table,
  FeedFetcher feedFetcher
) { }
