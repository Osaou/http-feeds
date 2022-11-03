package se.aourell.httpfeeds.publisher.core;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.spi.LocalFeedConsumerRegistry;
import se.aourell.httpfeeds.publisher.spi.DomainEventSerializer;
import se.aourell.httpfeeds.publisher.spi.EventBus;
import se.aourell.httpfeeds.publisher.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.publisher.spi.FeedItemRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EventBusImpl implements EventBus<Object> {

  private final String source;
  private final Optional<List<Class<?>>> deletionEventTypes;
  private final FeedItemRepository feedItemRepository;
  private final FeedItemIdGenerator feedItemIdGenerator;
  private final DomainEventSerializer domainEventSerializer;
  private final LocalFeedConsumerRegistry localFeedConsumerRegistry;

  public EventBusImpl(String source, Class<?> eventBaseType, FeedItemRepository feedItemRepository, FeedItemIdGenerator feedItemIdGenerator, DomainEventSerializer domainEventSerializer, LocalFeedConsumerRegistry localFeedConsumerRegistry) {
    this.source = source;
    this.feedItemRepository = feedItemRepository;
    this.feedItemIdGenerator = feedItemIdGenerator;
    this.domainEventSerializer = domainEventSerializer;
    this.localFeedConsumerRegistry = localFeedConsumerRegistry;

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
    final var feedItem = new FeedItem(id, type, source, time, subject, method, dataAsString);
    feedItemRepository.append(feedItem);

    // after persisting the event, we immediately go ahead and let local handlers process it
    localFeedConsumerRegistry.processLocalEvent(id, subject, event, time);
  }
}
