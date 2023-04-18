package se.aourell.httpfeeds.producer.core;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.producer.api.Version;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.producer.spi.EventBus;
import se.aourell.httpfeeds.producer.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EventBusImpl<TEvent> implements EventBus<TEvent> {

  private final String feedName;
  private final Optional<List<Class<?>>> deletionEventTypes;
  private final FeedItemRepository feedItemRepository;
  private final FeedItemIdGenerator feedItemIdGenerator;
  private final DomainEventSerializer domainEventSerializer;

  public EventBusImpl(String feedName, Class<TEvent> eventBaseType, FeedItemRepository feedItemRepository, FeedItemIdGenerator feedItemIdGenerator, DomainEventSerializer domainEventSerializer) {
    this.feedName = feedName;
    this.feedItemRepository = feedItemRepository;
    this.feedItemIdGenerator = feedItemIdGenerator;
    this.domainEventSerializer = domainEventSerializer;

    this.deletionEventTypes = eventBaseType.isSealed()
      ? Optional.of(Arrays.stream(eventBaseType.getPermittedSubclasses()).filter(EventUtil::isDeletionEvent).toList())
      : Optional.empty();
  }

  @Override
  public void publish(String subject, TEvent event, Instant time, String traceId) {
    final Class<?> eventType = event.getClass();
    final boolean isDeleteEvent = deletionEventTypes
      .map(types -> types.contains(eventType))
      .orElseGet(() -> EventUtil.isDeletionEvent(eventType));

    final String id = feedItemIdGenerator.generateId();
    final String type = eventType.getSimpleName();
    final int typeVersion = eventType.isAnnotationPresent(Version.class)
      ? eventType.getAnnotation(Version.class).value()
      : Version.DEFAULT;

    final String dataAsString = domainEventSerializer.toString(event);
    final String method = isDeleteEvent
      ? CloudEvent.DELETE_METHOD
      : null;

    // first persist the event
    // an exception here means we should not go ahead with local processing
    final var feedItem = new FeedItem(id, traceId, type, typeVersion, feedName, time, subject, method, dataAsString);
    feedItemRepository.append(feedItem);
  }
}
