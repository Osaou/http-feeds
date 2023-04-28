package se.aourell.httpfeeds.infrastructure.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.aourell.httpfeeds.consumer.spi.CloudEventDeserializer;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.util.Assert;

public class CloudEventDeserializerImpl implements CloudEventDeserializer {

  private final ObjectMapper cloudEventObjectMapper;

  public CloudEventDeserializerImpl(ObjectMapper cloudEventObjectMapper) {
    this.cloudEventObjectMapper = Assert.notNull(cloudEventObjectMapper);
  }

  @Override
  public CloudEvent toCloudEvent(String string) {
    try {
      return cloudEventObjectMapper.readValue(string, CloudEvent.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CloudEvent[] toCloudEvents(String string) {
    try {
      return cloudEventObjectMapper.readValue(string, CloudEvent[].class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
