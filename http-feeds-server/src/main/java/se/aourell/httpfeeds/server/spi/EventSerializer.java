package se.aourell.httpfeeds.server.spi;

public interface EventSerializer {

  String toString(Object object);

  Object toDomainEvent(String string);
}
