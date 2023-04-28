package se.aourell.httpfeeds.producer.core;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.producer.api.Version;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.producer.spi.EventBus;
import se.aourell.httpfeeds.producer.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;
import se.aourell.httpfeeds.util.Assert;

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
    this.feedName = Assert.hasStringValue(feedName);
    this.feedItemRepository = Assert.notNull(feedItemRepository);
    this.feedItemIdGenerator = Assert.notNull(feedItemIdGenerator);
    this.domainEventSerializer = Assert.notNull(domainEventSerializer);

    this.deletionEventTypes = Assert.notNull(eventBaseType).isSealed()
      ? Optional.of(Arrays.stream(eventBaseType.getPermittedSubclasses()).filter(EventUtil::isDeletionEvent).toList())
      : Optional.empty();
  }

  @Override
  public void publish(String subject, String traceId, TEvent event, Instant time) {
    Assert.hasStringValue(subject);
    Assert.hasStringValue(traceId);
    Assert.notNull(event);
    Assert.notNull(time);

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

    final var feedItem = new FeedItem(id, traceId, type, typeVersion, feedName, time, subject, method, dataAsString);
    feedItemRepository.append(feedItem);
  }
}
