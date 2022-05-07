package se.aourell.httpfeeds.core;

import com.github.f4b6a3.uuid.UuidCreator;
import se.aourell.httpfeeds.api.DeletionEvent;
import se.aourell.httpfeeds.spi.EventBus;
import se.aourell.httpfeeds.spi.EventSerializer;
import se.aourell.httpfeeds.spi.FeedItemRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class EventBusImpl implements EventBus<Object> {

  private final List<Class<?>> deletionEventTypes;
  private final FeedItemRepository feedItemRepository;
  private final EventSerializer eventSerializer;

  public EventBusImpl(Class<?> eventBaseType, FeedItemRepository feedItemRepository, EventSerializer eventSerializer) {
    this.feedItemRepository = feedItemRepository;
    this.eventSerializer = eventSerializer;

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
    final var dataAsString = eventSerializer.toString(event);

    feedItemRepository.append(id, type, time, subject, method, dataAsString);
  }
}
