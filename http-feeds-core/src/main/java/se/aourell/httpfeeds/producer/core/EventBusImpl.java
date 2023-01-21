package se.aourell.httpfeeds.producer.core;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.producer.spi.EventBus;
import se.aourell.httpfeeds.producer.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EventBusImpl implements EventBus<Object> {

  private final String feedName;
  private final Optional<List<Class<?>>> deletionEventTypes;
  private final FeedItemRepository feedItemRepository;
  private final FeedItemIdGenerator feedItemIdGenerator;
  private final DomainEventSerializer domainEventSerializer;

  public EventBusImpl(String feedName, Class<?> eventBaseType, FeedItemRepository feedItemRepository, FeedItemIdGenerator feedItemIdGenerator, DomainEventSerializer domainEventSerializer) {
    this.feedName = feedName;
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
    final var method = isDeleteEvent ? CloudEvent.DELETE_METHOD : null;
    final var dataAsString = domainEventSerializer.toString(event);

    // first persist the event
    // an exception here means we should not go ahead with local processing
    final var feedItem = new FeedItem(id, type, feedName, time, subject, method, dataAsString);
    feedItemRepository.append(feedItem);
  }
}
