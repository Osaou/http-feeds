package se.aourell.httpfeeds.server.core;

import se.aourell.httpfeeds.core.CloudEvent;
import se.aourell.httpfeeds.server.spi.EventBus;
import se.aourell.httpfeeds.server.spi.DomainEventSerializer;
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
  private final DomainEventSerializer domainEventSerializer;

  public EventBusImpl(Class<?> eventBaseType, HttpFeedDefinition feedDefinition, FeedItemRepository feedItemRepository, FeedItemIdGenerator feedItemIdGenerator, DomainEventSerializer domainEventSerializer) {
    this.feedDefinition = feedDefinition;
    this.feedItemRepository = feedItemRepository;
    this.feedItemIdGenerator = feedItemIdGenerator;
    this.domainEventSerializer = domainEventSerializer;

    this.deletionEventTypes = eventBaseType.isSealed()
      ? Optional.of(Arrays.stream(eventBaseType.getPermittedSubclasses()).filter(EventUtil::isDeletionEvent).toList())
      : Optional.empty();
  }

  @Override
  public void publish(String subject, Object event, Instant time) {
    final var eventType = event.getClass();
    final var isDeleteEvent = deletionEventTypes
      .map(types -> types.contains(eventType))
      .orElseGet(() -> EventUtil.isDeletionEvent(eventType));

    final var id = feedItemIdGenerator.generateId();
    final var type = eventType.getSimpleName();
    final var source = feedDefinition.path();
    final var method = isDeleteEvent ? CloudEvent.DELETE_METHOD : null;
    final var dataAsString = domainEventSerializer.toString(event);

    feedItemRepository.append(id, type, source, time, subject, method, dataAsString);
  }
}
