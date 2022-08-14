package se.aourell.httpfeeds.client.spi;

public interface DomainEventDeserializer {

  Object toDomainEvent(Object data, Class<?> type);
}
