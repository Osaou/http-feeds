package se.aourell.httpfeeds.producer.core;

import se.aourell.httpfeeds.producer.api.DeletionEvent;

public final class EventUtil {

  private EventUtil() { }

  static boolean isDeletionEvent(Class<?> eventType) {
    return eventType.isAnnotationPresent(DeletionEvent.class);
  }
}
