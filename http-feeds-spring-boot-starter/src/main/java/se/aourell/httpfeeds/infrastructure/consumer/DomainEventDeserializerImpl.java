package se.aourell.httpfeeds.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.util.Assert;

public class DomainEventDeserializerImpl implements DomainEventDeserializer {

  private final ObjectMapper domainEventMapper;

  public DomainEventDeserializerImpl(ObjectMapper domainEventMapper) {
    this.domainEventMapper = Assert.notNull(domainEventMapper);
  }

  @Override
  public Object toDomainEvent(Object data, Class<?> type) {
    return domainEventMapper.convertValue(data, type);
  }
}
