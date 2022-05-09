package se.aourell.httpfeeds.core;

import org.junit.jupiter.api.Test;
import se.aourell.httpfeeds.api.DeletionEvent;
import se.aourell.httpfeeds.spi.EventSerializer;
import se.aourell.httpfeeds.spi.FeedItemRepository;

import java.time.Instant;

import static org.mockito.Mockito.*;

class EventBusImplTest {

  private interface OkFeed {}
  private record OkFeed_Rec(int id) implements OkFeed {}
  @DeletionEvent
  private record OkFeed_Delete(String name) implements OkFeed {}

  private sealed interface OkFeedSealed permits OkFeedSealed_Rec, OkFeedSealed_Delete {}
  private record OkFeedSealed_Rec(int id) implements OkFeedSealed {}
  @DeletionEvent
  private record OkFeedSealed_Delete(String name) implements OkFeedSealed {}



  @Test
  void eventBusImpl_should_find_deletion_annotation_on_sealed_type() {
    final var feedDefinition = new HttpFeedDefinition(null, "path", null, null);
    final var feedItemRepository = mock(FeedItemRepository.class);
    final var eventSerializer = mock(EventSerializer.class);
    final var eventBus = new EventBusImpl(OkFeedSealed.class, feedDefinition, feedItemRepository, eventSerializer);

    final var time = Instant.MIN;
    final var deleteEvent = new OkFeedSealed_Delete("delete me");
    when(eventSerializer.toString(deleteEvent)).thenReturn("serialized");

    eventBus.publish("subject", deleteEvent, time);
    verify(feedItemRepository).append("uh-oh-this-is-random", OkFeedSealed_Delete.class.getName(), "path", time, "subject", "delete", "serialized");
  }
}
