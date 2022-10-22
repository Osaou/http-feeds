package se.aourell.httpfeeds.server.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.aourell.httpfeeds.publisher.api.EventFeed;
import se.aourell.httpfeeds.publisher.core.EventFeedRegistryImpl;
import se.aourell.httpfeeds.publisher.core.FeedItem;
import se.aourell.httpfeeds.publisher.spi.EventFeedRegistry;
import se.aourell.httpfeeds.publisher.spi.FeedItemService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventFeedRegistryImplTest {

  private EventFeedRegistry registry;

  @BeforeEach
  void beforeEach() {
    registry = new EventFeedRegistryImpl();
  }

  final FeedItemService serviceMock = new FeedItemService() {
    @Override
    public List<FeedItem> fetch(String lastEventId, String subjectId) {
      return null;
    }
    @Override
    public List<FeedItem> fetchWithTimeout(String lastEventId, String subjectId, Long timeoutMillis) {
      return null;
    }
  };

  @EventFeed(value ="test", persistenceName = "test_table")
  private record OkFeed(String id) {}

  @EventFeed(value ="other", persistenceName = "other_table")
  private record OkFeed2(String id) {}



  @Test
  void defineFeed_should_accept_valid_feed() {
    final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    final var definition = registry.defineFeed(path, serviceMock, false);
    Assertions.assertEquals("test", definition.name());
    Assertions.assertEquals("/feed/test", definition.feedPath());
    Assertions.assertEquals(serviceMock, definition.feedItemService());
    Assertions.assertFalse(definition.publishedHttpFeed());
  }



  @Test
  void getDefinedFeed_should_return_correct_feed() {
    final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    final var expected = registry.defineFeed(path, serviceMock, true);

    final var read = registry.getPublishedHttpFeed("/feed/test/");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());
  }

  @Test
  void getDefinedFeed_should_handle_nonstandard_path() {
    final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    final var expected = registry.defineFeed(path, serviceMock, true);

    final var read = registry.getPublishedHttpFeed("/feed/test");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());
  }

  @Test
  void getDefinedFeed_should_return_correct_feed_when_multiple_are_defined() {
    final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    final var expected = registry.defineFeed(path, serviceMock, true);

    final var feed2 = OkFeed2.class.getAnnotation(EventFeed.class);
    final var path2 = feed2.value();
    registry.defineFeed(path2, serviceMock, true);

    final var read = registry.getPublishedHttpFeed("/feed/test/");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());
  }

  @Test
  void getDefinedFeed_should_not_return_missing_definition() {
    final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    registry.defineFeed(path, serviceMock, true);

    final var read = registry.getPublishedHttpFeed("/feed/missing/");
    assertFalse(read.isPresent());
  }

  @Test
  void getDefinedFeed_should_not_return_unpublished_definition() {
    final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    registry.defineFeed(path, serviceMock, false);

    final var read = registry.getPublishedHttpFeed(path);
    assertFalse(read.isPresent());
  }
}
