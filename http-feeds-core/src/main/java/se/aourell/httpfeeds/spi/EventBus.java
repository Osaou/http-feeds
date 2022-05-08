package se.aourell.httpfeeds.spi;

import se.aourell.httpfeeds.api.DeletionEvent;

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
