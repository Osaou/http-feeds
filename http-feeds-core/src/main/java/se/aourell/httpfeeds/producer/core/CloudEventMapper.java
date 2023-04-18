package se.aourell.httpfeeds.producer.core;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;

import java.time.ZoneOffset;

public class CloudEventMapper {

  private final DomainEventSerializer domainEventSerializer;

  public CloudEventMapper(DomainEventSerializer domainEventSerializer) {
    this.domainEventSerializer = domainEventSerializer;
  }

  public CloudEvent mapFeedItem(FeedItem feedItem) {
    return CloudEvent.of(
      "1.0",
      feedItem.id(),
      feedItem.type(),
      feedItem.feedName(),
      feedItem.time().atOffset(ZoneOffset.UTC),
      feedItem.subject(),
      feedItem.method(),
      feedItem.method() != null ? null : "application/json",
      feedItem.method() != null ? null : domainEventSerializer.toDomainEvent(feedItem.data()),
      feedItem.traceId(),
      feedItem.typeVersion()
    );
  }
}
