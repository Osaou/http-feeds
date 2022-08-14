package se.aourell.httpfeeds.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.aourell.httpfeeds.client.spi.CloudEventArrayDeserializer;
import se.aourell.httpfeeds.core.CloudEvent;

public class CloudEventArrayDeserializerImpl implements CloudEventArrayDeserializer {

  private final ObjectMapper domainEventJsonMapper;

  public CloudEventArrayDeserializerImpl(ObjectMapper domainEventJsonMapper) {
    this.domainEventJsonMapper = domainEventJsonMapper;
  }

  @Override
  public CloudEvent[] toCloudEvents(String string) {
    try {
      return domainEventJsonMapper.readValue(string, CloudEvent[].class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
