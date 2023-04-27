package se.aourell.httpfeeds.tracing.core;

import se.aourell.httpfeeds.CloudEvent;

import java.time.OffsetDateTime;
import java.util.List;

public record ShelvedTrace(
  String traceId,
  String feedConsumerName,
  OffsetDateTime shelvedTime,
  String lastKnownError,
  boolean isRedelivering,
  List<CloudEvent> events
) { }
