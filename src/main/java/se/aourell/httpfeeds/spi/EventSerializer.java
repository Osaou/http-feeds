package se.aourell.httpfeeds.spi;

public interface EventSerializer {
  String toString(Object object);

  Object toEvent(String string);
}
