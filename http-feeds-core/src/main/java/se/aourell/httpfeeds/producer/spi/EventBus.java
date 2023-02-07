package se.aourell.httpfeeds.producer.spi;

import java.time.Instant;

public interface EventBus<TEvent> {

  default
  void publish(String subject, TEvent event) {
    publish(subject, event, Instant.now());
  }

  void publish(String subject, TEvent event, Instant time);
}
