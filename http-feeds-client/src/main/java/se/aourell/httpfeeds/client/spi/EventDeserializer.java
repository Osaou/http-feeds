package se.aourell.httpfeeds.client.spi;

public interface EventDeserializer {

  Object toDomainEvent(Object data, Class<?> type);
}
