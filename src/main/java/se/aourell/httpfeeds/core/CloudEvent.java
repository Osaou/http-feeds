package se.aourell.httpfeeds.core;

import se.aourell.httpfeeds.spi.EventSerializer;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record CloudEvent(String specversion, String id, String type, OffsetDateTime time, String subject, String method, String datacontenttype, Object data) {

  public static class Mapper {

    private final EventSerializer eventSerializer;

    public Mapper(EventSerializer eventSerializer) {
      this.eventSerializer = eventSerializer;
    }

    public CloudEvent mapFeedItem(FeedItem feedItem) {
      return new CloudEvent(
        "1.0",
        feedItem.getId(),
        feedItem.getType(),
        feedItem.getTime().atOffset(ZoneOffset.UTC),
        feedItem.getSubject(),
        feedItem.getMethod(),
        feedItem.getMethod() != null ? null : "application/json",
        feedItem.getMethod() != null ? null : eventSerializer.toEvent(feedItem.getData())
      );
    }
  }
}
