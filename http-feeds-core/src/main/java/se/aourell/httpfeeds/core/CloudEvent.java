package se.aourell.httpfeeds.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.aourell.httpfeeds.spi.EventSerializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public record CloudEvent(String specversion, String id, String type, String source, @JsonSerialize(using = OffsetDateTimeSerializer.class) OffsetDateTime time, String subject, String method, String datacontenttype, Object data) {

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
        feedItem.method() != null ? null : eventSerializer.toEvent(feedItem.data())
      );
    }
  }

  public static class OffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {

    private static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx")
      .withZone(ZoneId.of("UTC"));

    @Override
    public void serialize(OffsetDateTime value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
      if (value == null) {
        throw new IllegalArgumentException("OffsetDateTime argument is null");
      }
      jsonGenerator.writeString(ISO_8601_FORMATTER.format(value));
    }
  }
}
