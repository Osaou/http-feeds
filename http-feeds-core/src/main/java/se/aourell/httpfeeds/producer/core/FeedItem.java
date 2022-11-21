package se.aourell.httpfeeds.producer.core;

import java.time.Instant;

public record FeedItem(String id, String type, String feedName, Instant time, String subject, String method, String data) { }
