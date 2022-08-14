package se.aourell.httpfeeds.server.core;

import java.time.Instant;

public record FeedItem(String id, String type, String source, Instant time, String subject, String method, String data) { }
