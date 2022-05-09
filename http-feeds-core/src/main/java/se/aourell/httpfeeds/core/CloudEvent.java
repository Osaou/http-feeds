package se.aourell.httpfeeds.core;

import se.aourell.httpfeeds.spi.EventSerializer;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record CloudEvent(String specversion, String id, String type, String source, OffsetDateTime time, String subject, String method, String datacontenttype, Object data) {

  public static class Mapper {

    private final EventSerializer eventSerializer;

    public Mapper(EventSerializer eventSerializer) {
      this.eventSerializer = eventSerializer;
    }

    public CloudEvent mapFeedItem(FeedItem feedItem) {
      return new CloudEvent(
        "1.0",
        feedItem.id(),
        feedItem.type(),
        feedItem.source(),
        feedItem.time().atOffset(ZoneOffset.UTC),
        feedItem.subject(),
        feedItem.method(),
        feedItem.method() != null ? null : "application/json",
        feedItem.method() != null ? null : eventSerializer.toDomainEvent(feedItem.data())
      );
    }
  }
}
