package se.aourell.httpfeeds.infrastructure.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.aourell.httpfeeds.spi.EventSerializer;

public class EventSerializerImpl implements EventSerializer {

  private final ObjectMapper objectMapper;

  public EventSerializerImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String toString(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object toEvent(String string) {
    try {
      return objectMapper.readValue(string, Object.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
