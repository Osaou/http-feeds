package se.aourell.httpfeeds.infrastructure.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.producer.spi.CloudEventSerializer;
import se.aourell.httpfeeds.util.Assert;

import java.util.List;

public class CloudEventSerializerImpl implements CloudEventSerializer {

  private final ObjectMapper cloudEventObjectMapper;

  public CloudEventSerializerImpl(ObjectMapper cloudEventObjectMapper) {
    this.cloudEventObjectMapper = Assert.notNull(cloudEventObjectMapper);
  }

  @Override
  public String toString(CloudEvent event) {
    try {
      return cloudEventObjectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString(List<CloudEvent> events) {
    try {
      return cloudEventObjectMapper.writeValueAsString(events);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
