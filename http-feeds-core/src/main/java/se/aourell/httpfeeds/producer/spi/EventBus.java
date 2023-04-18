package se.aourell.httpfeeds.producer.spi;

import java.time.Instant;

public interface EventBus<TEvent> {

  default
  void publish(String subject, TEvent event) {
    publish(subject, event, Instant.now());
  }

  default
  void publish(String subject, TEvent event, Instant time) {
    publish(subject, event, time, subject);
  }

  default
  void publish(String subject, TEvent event, String traceId) {
    publish(subject, event, Instant.now(), traceId);
  }

  void publish(String subject, TEvent event, Instant time, String traceId);
}
