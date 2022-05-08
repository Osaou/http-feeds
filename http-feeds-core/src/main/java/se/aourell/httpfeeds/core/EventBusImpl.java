package se.aourell.httpfeeds.core;

import com.github.f4b6a3.uuid.UuidCreator;
import se.aourell.httpfeeds.spi.EventBus;
import se.aourell.httpfeeds.spi.EventSerializer;
import se.aourell.httpfeeds.spi.FeedItemRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EventBusImpl implements EventBus<Object> {

  private final Optional<List<Class<?>>> deletionEventTypes;
  private final FeedItemRepository feedItemRepository;
  private final EventSerializer eventSerializer;

  public EventBusImpl(Class<?> eventBaseType, FeedItemRepository feedItemRepository, EventSerializer eventSerializer) {
    this.feedItemRepository = feedItemRepository;
    this.eventSerializer = eventSerializer;

    this.deletionEventTypes = eventBaseType.isSealed()
      ? Optional.of(Arrays.stream(eventBaseType.getPermittedSubclasses()).filter(EventBus::isDeletionEvent).toList())
      : Optional.empty();
  }

  @Override
  public void publish(String subject, Object event, Instant time) {
    final var eventType = event.getClass();
    final var isDeleteEvent = deletionEventTypes
      .map(types -> types.contains(eventType))
      .orElseGet(() -> EventBus.isDeletionEvent(eventType));

    final var id = UuidCreator.getTimeOrderedWithRandom().toString();
    final var type = eventType.getName();
    final var method = isDeleteEvent ? "delete" : null;
    final var dataAsString = eventSerializer.toString(event);

    feedItemRepository.append(id, type, time, subject, method, dataAsString);
  }
}
