package se.aourell.httpfeeds.server.core;

import org.junit.jupiter.api.Test;
import se.aourell.httpfeeds.consumer.spi.HttpFeedConsumerRegistry;
import se.aourell.httpfeeds.consumer.spi.LocalFeedConsumerRegistry;
import se.aourell.httpfeeds.publisher.api.DeletionEvent;
import se.aourell.httpfeeds.publisher.core.EventBusImpl;
import se.aourell.httpfeeds.publisher.spi.DomainEventSerializer;
import se.aourell.httpfeeds.publisher.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.publisher.spi.FeedItemRepository;

import java.time.Instant;

import static org.mockito.Mockito.*;

class EventBusImplTest {

  // sealed class hierarchy / ADT
  private sealed interface OkFeedSealed permits OkFeedSealed_Rec, OkFeedSealed_Delete {}
  private record OkFeedSealed_Rec(int id) implements OkFeedSealed {}
  @DeletionEvent
  private record OkFeedSealed_Delete(String name) implements OkFeedSealed {}

  @Test
  void eventBusImpl_should_serialize_data_for_sealed_type() {
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var localFeedConsumerRegistry = mock(LocalFeedConsumerRegistry.class);
    final var eventBus = new EventBusImpl(OkFeedSealed.class, feedItemRepository, feedItemIdGenerator, eventSerializer, localFeedConsumerRegistry);

    final var time = Instant.MIN;
    final var deleteEvent = new OkFeedSealed_Rec(1);

    when(feedItemIdGenerator.generateId()).thenReturn("random id");
    when(eventSerializer.toString(deleteEvent)).thenReturn("serialized data");

    eventBus.publish("subject", deleteEvent, time);
    verify(feedItemRepository).append("random id", OkFeedSealed_Rec.class.getSimpleName(), time, "subject", null, "serialized data");
  }

  @Test
  void eventBusImpl_should_find_deletion_annotation_on_sealed_type() {
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var localFeedConsumerRegistry = mock(LocalFeedConsumerRegistry.class);
    final var eventBus = new EventBusImpl(OkFeedSealed.class, feedItemRepository, feedItemIdGenerator, eventSerializer, localFeedConsumerRegistry);

    final var time = Instant.MIN;
    final var deleteEvent = new OkFeedSealed_Delete("delete me");

    when(feedItemIdGenerator.generateId()).thenReturn("random id");
    when(eventSerializer.toString(deleteEvent)).thenReturn("serialized data");

    eventBus.publish("subject", deleteEvent, time);
    verify(feedItemRepository).append("random id", OkFeedSealed_Delete.class.getSimpleName(), time, "subject", "delete", "serialized data");
  }



  // classic OOP class hierarchy
  private interface OkFeed {}
  private record OkFeed_Rec(int id) implements OkFeed {}
  @DeletionEvent
  private record OkFeed_Delete(String name) implements OkFeed {}

  @Test
  void eventBusImpl_should_serialize_data_for_classic_types() {
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var localFeedConsumerRegistry = mock(LocalFeedConsumerRegistry.class);
    final var eventBus = new EventBusImpl(OkFeed.class, feedItemRepository, feedItemIdGenerator, eventSerializer, localFeedConsumerRegistry);

    final var time = Instant.MIN;
    final var deleteEvent = new OkFeed_Rec(1);

    when(feedItemIdGenerator.generateId()).thenReturn("random id");
    when(eventSerializer.toString(deleteEvent)).thenReturn("serialized data");

    eventBus.publish("subject", deleteEvent, time);
    verify(feedItemRepository).append("random id", OkFeed_Rec.class.getSimpleName(), time, "subject", null, "serialized data");
  }

  @Test
  void eventBusImpl_should_find_deletion_annotation_on_classic_types() {
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var localFeedConsumerRegistry = mock(LocalFeedConsumerRegistry.class);
    final var eventBus = new EventBusImpl(OkFeed.class, feedItemRepository, feedItemIdGenerator, eventSerializer, localFeedConsumerRegistry);

    final var time = Instant.MIN;
    final var deleteEvent = new OkFeed_Delete("delete me");

    when(feedItemIdGenerator.generateId()).thenReturn("random id");
    when(eventSerializer.toString(deleteEvent)).thenReturn("serialized data");

    eventBus.publish("subject", deleteEvent, time);
    verify(feedItemRepository).append("random id", OkFeed_Delete.class.getSimpleName(), time, "subject", "delete", "serialized data");
  }
}
