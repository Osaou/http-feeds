package se.aourell.httpfeeds.infrastructure.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.util.Assert;

public class DomainEventSerializerImpl implements DomainEventSerializer {

  private final ObjectMapper domainEventObjectMapper;

  public DomainEventSerializerImpl(ObjectMapper domainEventObjectMapper) {
    this.domainEventObjectMapper = Assert.notNull(domainEventObjectMapper);
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
