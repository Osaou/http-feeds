package se.aourell.httpfeeds.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.aourell.httpfeeds.client.spi.DomainEventDeserializer;

public class DomainEventDeserializerImpl implements DomainEventDeserializer {

  private final ObjectMapper domainEventMapper;

  public DomainEventDeserializerImpl(ObjectMapper domainEventMapper) {
    this.domainEventMapper = domainEventMapper;
  }

  @Override
  public Object toDomainEvent(Object data, Class<?> type) {
    return domainEventMapper.convertValue(data, type);
  }
}
