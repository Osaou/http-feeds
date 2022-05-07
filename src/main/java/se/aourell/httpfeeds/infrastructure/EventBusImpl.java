package se.aourell.httpfeeds.infrastructure;

import com.github.f4b6a3.uuid.UuidCreator;
import se.aourell.httpfeeds.api.DeletionEvent;
import se.aourell.httpfeeds.core.EventSerializer;
import se.aourell.httpfeeds.spi.EventBus;
import se.aourell.httpfeeds.spi.FeedRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class EventBusImpl implements EventBus<Object> {

  private final List<Class<?>> deletionEventTypes;
  private final FeedRepository repository;

  public EventBusImpl(Class<?> eventBaseType, FeedRepository repository) {
    this.repository = repository;

    if (eventBaseType.isSealed()) {
      // for sealed types we can precompute all possible event types that are for deletion
      this.deletionEventTypes = Arrays.stream(eventBaseType.getPermittedSubclasses())
        .filter(subclass -> Arrays.stream(subclass.getAnnotations()).anyMatch(annotation -> annotation.annotationType() == DeletionEvent.class))
        .toList();
    }
    else {
      this.deletionEventTypes = null;
    }
  }

  @Override
  public void publish(String subject, Object event, Instant time) {
    final var isDeleteEvent = deletionEventTypes != null
      ? deletionEventTypes.contains(event.getClass())
      : Arrays.stream(event.getClass().getAnnotations()).anyMatch(annotation -> annotation.annotationType() == DeletionEvent.class);

    final var id = UuidCreator.getTimeOrderedWithRandom().toString();
    final var type = event.getClass().getName();
    final var method = isDeleteEvent ? "delete" : null;
    final var dataAsString = EventSerializer.toString(event);

    repository.append(id, type, time, subject, method, dataAsString);
  }
}
