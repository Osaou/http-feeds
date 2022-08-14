package se.aourell.httpfeeds.server.spi;

public interface DomainEventSerializer {

  String toString(Object object);

  Object toDomainEvent(String string);
}
