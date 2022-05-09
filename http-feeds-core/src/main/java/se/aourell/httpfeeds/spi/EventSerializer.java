package se.aourell.httpfeeds.spi;

public interface EventSerializer {

  String toString(Object object);

  Object toDomainEvent(String string);
}
