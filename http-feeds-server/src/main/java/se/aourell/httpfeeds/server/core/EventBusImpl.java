package se.aourell.httpfeeds.server.core;

import se.aourell.httpfeeds.core.CloudEvent;
import se.aourell.httpfeeds.server.spi.EventBus;
import se.aourell.httpfeeds.server.spi.EventSerializer;
import se.aourell.httpfeeds.server.spi.FeedItemRepository;
import se.aourell.httpfeeds.server.spi.FeedItemIdGenerator;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EventBusImpl implements EventBus<Object> {

  private final Optional<List<Class<?>>> deletionEventTypes;
  private final HttpFeedDefinition feedDefinition;
  private final FeedItemRepository feedItemRepository;
  private final FeedItemIdGenerator feedItemIdGenerator;
  private final EventSerializer eventSerializer;

  public EventBusImpl(Class<?> eventBaseType, HttpFeedDefinition feedDefinition, FeedItemRepository feedItemRepository, FeedItemIdGenerator feedItemIdGenerator, EventSerializer eventSerializer) {
    this.feedDefinition = feedDefinition;
    this.feedItemRepository = feedItemRepository;
    this.feedItemIdGenerator = feedItemIdGenerator;
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

    final var id = feedItemIdGenerator.generateId();
    final var type = eventType.getName();
    final var source = feedDefinition.path();
    final var method = isDeleteEvent ? CloudEvent.DELETE_METHOD : null;
    final var dataAsString = eventSerializer.toString(event);

    feedItemRepository.append(id, type, source, time, subject, method, dataAsString);
  }
}
