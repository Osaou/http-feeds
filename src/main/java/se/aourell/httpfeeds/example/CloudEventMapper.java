package se.aourell.httpfeeds.example;

import java.time.ZoneOffset;

public class CloudEventMapper {
  public static CloudEvent toCloudEvent(FeedItem feedItem) {
    return new CloudEvent(
      "1.0",
      feedItem.getId(),
      feedItem.getType(),
      feedItem.getTime().atOffset(ZoneOffset.UTC),
      feedItem.getSubject(),
      feedItem.getMethod(),
      feedItem.getMethod() != null ? null : "application/json",
      feedItem.getMethod() != null ? null : DataSerializer.toObject(feedItem.getData())
    );
  }
}
