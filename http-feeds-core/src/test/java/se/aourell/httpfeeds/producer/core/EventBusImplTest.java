package se.aourell.httpfeeds.producer.core;

import org.junit.jupiter.api.Test;
import se.aourell.httpfeeds.producer.api.DeletionEvent;
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
    /*
     * GIVEN
     */

    final var subject = "subject";
    final var traceId = "trace id";
    final var time = Instant.MIN;
    final var feedName = "feedName";
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var eventBus = new EventBusImpl<>(feedName, OkFeedSealed.class, feedItemRepository, feedItemIdGenerator, eventSerializer);
    final var deleteEvent = new OkFeedSealed_Rec(1);

    when(feedItemIdGenerator.generateId())
      .thenReturn("random id");

    when(eventSerializer.toString(deleteEvent))
      .thenReturn("serialized data");

    /*
     * WHEN
     */

    eventBus.publish(subject, traceId, deleteEvent, time);

    /*
     * THEN
     */

    final var feedItem = new FeedItem("random id", traceId, OkFeedSealed_Rec.class.getSimpleName(), 1, feedName, time, subject, null, "serialized data");
    verify(feedItemRepository).append(feedItem);
  }

  @Test
  void eventBusImpl_should_find_deletion_annotation_on_sealed_type() {
    /*
     * GIVEN
     */

    final var subject = "subject";
    final var traceId = "trace id";
    final var time = Instant.MIN;
    final var feedName = "feedName";
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var eventBus = new EventBusImpl<>(feedName, OkFeedSealed.class, feedItemRepository, feedItemIdGenerator, eventSerializer);
    final var deleteEvent = new OkFeedSealed_Delete("delete me");

    when(feedItemIdGenerator.generateId())
      .thenReturn("random id");

    when(eventSerializer.toString(deleteEvent))
      .thenReturn("serialized data");

    /*
     * WHEN
     */

    eventBus.publish(subject, traceId, deleteEvent, time);

    /*
     * THEN
     */

    final var feedItem = new FeedItem("random id", traceId, OkFeedSealed_Delete.class.getSimpleName(), 1, feedName, time, subject, "delete", "serialized data");
    verify(feedItemRepository).append(feedItem);
  }



  // classic OOP class hierarchy
  private interface OkFeed {}
  private record OkFeed_Rec(int id) implements OkFeed {}
  @DeletionEvent
  private record OkFeed_Delete(String name) implements OkFeed {}

  @Test
  void eventBusImpl_should_serialize_data_for_classic_types() {
    /*
     * GIVEN
     */

    final var subject = "subject";
    final var traceId = "trace id";
    final var time = Instant.MIN;
    final var feedName = "feedName";
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var eventBus = new EventBusImpl<>(feedName, OkFeed.class, feedItemRepository, feedItemIdGenerator, eventSerializer);
    final var deleteEvent = new OkFeed_Rec(1);

    when(feedItemIdGenerator.generateId())
      .thenReturn("random id");

    when(eventSerializer.toString(deleteEvent))
      .thenReturn("serialized data");

    /*
     * WHEN
     */

    eventBus.publish(subject, traceId, deleteEvent, time);

    /*
     * THEN
     */

    final var feedItem = new FeedItem("random id", traceId, OkFeed_Rec.class.getSimpleName(), 1, feedName, time, subject, null, "serialized data");
    verify(feedItemRepository).append(feedItem);
  }

  @Test
  void eventBusImpl_should_find_deletion_annotation_on_classic_types() {
    /*
     * GIVEN
     */

    final var subject = "subject";
    final var traceId = "trace id";
    final var time = Instant.MIN;
    final var feedName = "feedName";
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var feedItemIdGenerator = mock(FeedItemIdGenerator.class);
    final var eventSerializer = mock(DomainEventSerializer.class);
    final var eventBus = new EventBusImpl<>(feedName, OkFeed.class, feedItemRepository, feedItemIdGenerator, eventSerializer);
    final var deleteEvent = new OkFeed_Delete("delete me");

    when(feedItemIdGenerator.generateId())
      .thenReturn("random id");

    when(eventSerializer.toString(deleteEvent))
      .thenReturn("serialized data");

    /*
     * WHEN
     */

    eventBus.publish(subject, traceId, deleteEvent, time);

    /*
     * THEN
     */

    final var feedItem = new FeedItem("random id", traceId, OkFeed_Delete.class.getSimpleName(), 1, feedName, time, subject, "delete", "serialized data");
    verify(feedItemRepository).append(feedItem);
  }
}
