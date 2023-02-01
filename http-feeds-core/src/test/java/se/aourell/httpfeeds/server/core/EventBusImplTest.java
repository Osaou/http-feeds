package se.aourell.httpfeeds.server.core;

import org.junit.jupiter.api.Test;
import se.aourell.httpfeeds.consumer.spi.LocalFeedConsumerRegistry;
import se.aourell.httpfeeds.producer.api.DeletionEvent;
import se.aourell.httpfeeds.producer.core.EventBusImpl;
import se.aourell.httpfeeds.producer.core.FeedItem;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.producer.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventBusImplTest {

  // sealed class hierarchy / ADT
  private sealed interface OkFeedSealed permits OkFeedSealed_Rec, OkFeedSealed_Delete {}
  private record OkFeedSealed_Rec(int id) implements OkFeedSealed {}
  @DeletionEvent
  private record OkFeedSealed_Delete(String name) implements OkFeedSealed {}

  @Test
  void eventBusImpl_should_serialize_data_for_sealed_type() {
    // given
    final var feedName = "feedName";
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var eventBus = new EventBusImpl(feedName, OkFeedSealed.class, feedItemRepository, feedItemIdGenerator, eventSerializer);
    final var deleteEvent = new OkFeedSealed_Rec(1);

    when(feedItemIdGenerator.generateId()).thenReturn("random id");
    when(eventSerializer.toString(deleteEvent)).thenReturn("serialized data");

    // when
    final var time = Instant.MIN;
    eventBus.publish("subject", deleteEvent, time);

    // then
    final var feedItem = new FeedItem("random id", OkFeedSealed_Rec.class.getSimpleName(), feedName, time, "subject", null, "serialized data");
    verify(feedItemRepository).append(feedItem);
  }

  @Test
  void eventBusImpl_should_find_deletion_annotation_on_sealed_type() {
    // given
    final var feedName = "feedName";
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var eventBus = new EventBusImpl(feedName, OkFeedSealed.class, feedItemRepository, feedItemIdGenerator, eventSerializer);
    final var deleteEvent = new OkFeedSealed_Delete("delete me");

    when(feedItemIdGenerator.generateId()).thenReturn("random id");
    when(eventSerializer.toString(deleteEvent)).thenReturn("serialized data");

    // when
    final var time = Instant.MIN;
    eventBus.publish("subject", deleteEvent, time);

    // then
    final var feedItem = new FeedItem("random id", OkFeedSealed_Delete.class.getSimpleName(), feedName, time, "subject", "delete", "serialized data");
    verify(feedItemRepository).append(feedItem);
  }



  // classic OOP class hierarchy
  private interface OkFeed {}
  private record OkFeed_Rec(int id) implements OkFeed {}
  @DeletionEvent
  private record OkFeed_Delete(String name) implements OkFeed {}

  @Test
  void eventBusImpl_should_serialize_data_for_classic_types() {
    // given
    final var feedName = "feedName";
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var eventBus = new EventBusImpl(feedName, OkFeed.class, feedItemRepository, feedItemIdGenerator, eventSerializer);
    final var deleteEvent = new OkFeed_Rec(1);

    when(feedItemIdGenerator.generateId()).thenReturn("random id");
    when(eventSerializer.toString(deleteEvent)).thenReturn("serialized data");

    // when
    final var time = Instant.MIN;
    eventBus.publish("subject", deleteEvent, time);

    // then
    final var feedItem = new FeedItem("random id", OkFeed_Rec.class.getSimpleName(), feedName, time, "subject", null, "serialized data");
    verify(feedItemRepository).append(feedItem);
  }

  @Test
  void eventBusImpl_should_find_deletion_annotation_on_classic_types() {
    // given
    final var feedName = "feedName";
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var eventBus = new EventBusImpl(feedName, OkFeed.class, feedItemRepository, feedItemIdGenerator, eventSerializer);
    final var deleteEvent = new OkFeed_Delete("delete me");

    when(feedItemIdGenerator.generateId()).thenReturn("random id");
    when(eventSerializer.toString(deleteEvent)).thenReturn("serialized data");

    // when
    final var time = Instant.MIN;
    eventBus.publish("subject", deleteEvent, time);

    // then
    final var feedItem = new FeedItem("random id", OkFeed_Delete.class.getSimpleName(), feedName, time, "subject", "delete", "serialized data");
    verify(feedItemRepository).append(feedItem);
  }
}