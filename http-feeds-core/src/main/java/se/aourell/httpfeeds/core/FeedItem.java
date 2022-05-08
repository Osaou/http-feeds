package se.aourell.httpfeeds.core;

import java.time.Instant;

public record FeedItem(String id, String type, Instant time, String subject, String method, String data) { }
