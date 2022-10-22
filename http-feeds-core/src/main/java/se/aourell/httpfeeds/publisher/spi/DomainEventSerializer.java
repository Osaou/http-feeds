package se.aourell.httpfeeds.publisher.spi;

public interface DomainEventSerializer {

  String toString(Object object);

  Object toDomainEvent(String string);
}
