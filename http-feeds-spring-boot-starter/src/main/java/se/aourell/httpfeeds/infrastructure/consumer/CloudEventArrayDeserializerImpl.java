package se.aourell.httpfeeds.infrastructure.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.aourell.httpfeeds.consumer.spi.CloudEventArrayDeserializer;
import se.aourell.httpfeeds.CloudEvent;

public class CloudEventArrayDeserializerImpl implements CloudEventArrayDeserializer {

  private final ObjectMapper domainEventObjectMapper;

  public CloudEventArrayDeserializerImpl(ObjectMapper domainEventObjectMapper) {
    this.domainEventObjectMapper = domainEventObjectMapper;
  }

  @Override
  public CloudEvent[] toCloudEvents(String string) {
    try {
      return domainEventObjectMapper.readValue(string, CloudEvent[].class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
