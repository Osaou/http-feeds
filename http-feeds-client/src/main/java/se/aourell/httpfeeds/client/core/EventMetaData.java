package se.aourell.httpfeeds.client.core;

import java.time.OffsetDateTime;

public record EventMetaData(String specversion, String id, String source, OffsetDateTime time) { }
