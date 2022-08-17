package se.aourell.httpfeeds.server.core;

import se.aourell.httpfeeds.server.api.DeletionEvent;

import java.util.Arrays;

public final class EventUtil {

  private EventUtil() { }

  static boolean isDeletionEvent(Class<?> eventType) {
    return Arrays.stream(eventType.getAnnotations()).anyMatch(annotation -> annotation.annotationType() == DeletionEvent.class);
  }
}
