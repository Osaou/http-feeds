package se.aourell.httpfeeds.producer.spi;

import java.time.Instant;

public interface EventBus<TEvent> {

  default
  void publish(String subject, TEvent event) {
    publish(subject, subject, event, Instant.now());
  }

  default
  void publish(String subject, TEvent event, Instant time) {
    publish(subject, subject, event, time);
  }

  default
  void publish(String subject, String traceId, TEvent event) {
    publish(subject, traceId, event, Instant.now());
  }

  void publish(String subject, String traceId, TEvent event, Instant time);
}
