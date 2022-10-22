package se.aourell.httpfeeds.consumer.spi;

public interface DomainEventDeserializer {

  Object toDomainEvent(Object data, Class<?> type);
}
