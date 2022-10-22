package se.aourell.httpfeeds.consumer.core;

import java.time.OffsetDateTime;

public record EventMetaData(String eventId, String subjectId, OffsetDateTime timestamp, String source) { }
