package se.aourell.httpfeeds.server.spi;

import se.aourell.httpfeeds.server.api.DeletionEvent;

import java.time.Instant;
import java.util.Arrays;

public interface EventBus<TEvent> {

  void publish(String subject, TEvent event, Instant time);

  default void publish(String subject, TEvent event) {
    publish(subject, event, Instant.now());
  }

  static boolean isDeletionEvent(Class<?> eventType) {
    return Arrays.stream(eventType.getAnnotations()).anyMatch(annotation -> annotation.annotationType() == DeletionEvent.class);
  }
}
