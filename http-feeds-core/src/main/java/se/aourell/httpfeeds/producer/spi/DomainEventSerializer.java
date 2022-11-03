package se.aourell.httpfeeds.producer.spi;

public interface DomainEventSerializer {

  String toString(Object object);

  Object toDomainEvent(String string);
}
