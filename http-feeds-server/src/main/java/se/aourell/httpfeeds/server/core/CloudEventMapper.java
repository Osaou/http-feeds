package se.aourell.httpfeeds.server.core;

import se.aourell.httpfeeds.core.CloudEvent;
import se.aourell.httpfeeds.server.spi.EventSerializer;

import java.time.ZoneOffset;

public class CloudEventMapper {

  private final EventSerializer eventSerializer;

  public CloudEventMapper(EventSerializer eventSerializer) {
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
