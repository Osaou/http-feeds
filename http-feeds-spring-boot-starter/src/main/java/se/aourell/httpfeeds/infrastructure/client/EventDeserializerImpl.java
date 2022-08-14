package se.aourell.httpfeeds.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.aourell.httpfeeds.client.spi.EventDeserializer;

public class EventDeserializerImpl implements EventDeserializer {

  private final ObjectMapper domainEventMapper;

  public EventDeserializerImpl(ObjectMapper domainEventMapper) {
    this.domainEventMapper = domainEventMapper;
  }

  @Override
  public Object toDomainEvent(Object data, Class<?> type) {
    return domainEventMapper.convertValue(data, type);
  }
}
