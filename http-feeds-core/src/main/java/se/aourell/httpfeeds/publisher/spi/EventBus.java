package se.aourell.httpfeeds.publisher.spi;

import java.time.Instant;

public interface EventBus<TEvent> {

  void publish(String subject, TEvent event, Instant time);

  default void publish(String subject, TEvent event) {
    publish(subject, event, Instant.now());
  }
}
