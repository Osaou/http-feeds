package se.aourell.httpfeeds.example;

import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.Arrays;

public class EventBusImpl implements EventBus<Object> {

  private final FeedRepository repository;

  public EventBusImpl(FeedRepository repository) {
    this.repository = repository;
  }

  @Override
  public void publish(String subject, Object event, Instant time) {
    final var id = UuidCreator.getTimeOrderedWithRandom().toString();
    final var type = event.getClass().getName();
    final var method = Arrays.stream(event.getClass().getAnnotations()).filter(a -> a.annotationType() == Delete.class).map(a -> "delete").findAny().orElse(null);
    final var dataAsString = DataSerializer.toString(event);

    repository.append(id, type, time, subject, method, dataAsString);
  }
}
