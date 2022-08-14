package se.aourell.httpfeeds.server.core;

import se.aourell.httpfeeds.core.CloudEvent;
import se.aourell.httpfeeds.server.spi.DomainEventSerializer;

import java.time.ZoneOffset;

public class CloudEventMapper {

  private final DomainEventSerializer domainEventSerializer;

  public CloudEventMapper(DomainEventSerializer domainEventSerializer) {
    this.domainEventSerializer = domainEventSerializer;
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
      feedItem.method() != null ? null : domainEventSerializer.toDomainEvent(feedItem.data())
    );
  }
}
