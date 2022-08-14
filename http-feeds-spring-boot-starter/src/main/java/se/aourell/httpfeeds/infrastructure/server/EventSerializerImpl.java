package se.aourell.httpfeeds.infrastructure.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.aourell.httpfeeds.server.spi.EventSerializer;

public class EventSerializerImpl implements EventSerializer {

  private final ObjectMapper domainEventObjectMapper;

  public EventSerializerImpl(ObjectMapper domainEventObjectMapper) {
    this.domainEventObjectMapper = domainEventObjectMapper;
  }

  @Override
  public String toString(Object object) {
    try {
      return domainEventObjectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object toDomainEvent(String string) {
    try {
      return domainEventObjectMapper.readValue(string, Object.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
