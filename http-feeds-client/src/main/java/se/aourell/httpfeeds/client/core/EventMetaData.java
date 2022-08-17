package se.aourell.httpfeeds.client.core;

import java.time.OffsetDateTime;

public record EventMetaData(String eventId, String subjectId, OffsetDateTime timestamp, String source) { }
