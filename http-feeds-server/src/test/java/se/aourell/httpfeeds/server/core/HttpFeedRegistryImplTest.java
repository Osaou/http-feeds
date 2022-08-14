package se.aourell.httpfeeds.server.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.aourell.httpfeeds.server.api.HttpFeed;
import se.aourell.httpfeeds.server.spi.FeedItemService;
import se.aourell.httpfeeds.server.spi.HttpFeedRegistry;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HttpFeedRegistryImplTest {

  private HttpFeedRegistry registry;

  @BeforeEach
  void beforeEach() {
    registry = new HttpFeedRegistryImpl();
  }

  final FeedItemService serviceMock = new FeedItemService() {
    @Override
    public List<FeedItem> fetch(Optional<String> lastEventId) {
      return null;
    }
    @Override
    public List<FeedItem> fetchWithTimeout(Optional<String> lastEventId, Long timeoutMillis) {
      return null;
    }
  };

  @HttpFeed(path="/feed/test/", feedName = "test", persistenceName = "test_table")
  private record OkFeed(String id) {}

  @HttpFeed(path="/feed/two/", feedName = "two", persistenceName = "two_table")
  private record OkFeed2(String id) {}

  @HttpFeed(path="/feed/test", feedName = "test", persistenceName = "test_table")
  private record OkFeed_NonStandardPath(String id) {}

  @HttpFeed(path="/test", feedName = "test", persistenceName = "test_table")
  private record ErrorFeed_MissingPathPrefix(String id) {}

  @HttpFeed(path="/", feedName = "test", persistenceName = "test_table")
  private record ErrorFeed_EmptyPath(String id) {}

  @HttpFeed(path="", feedName = "test", persistenceName = "test_table")
  private record ErrorFeed_MissingPath(String id) {}

  @HttpFeed(path="/feed/test", feedName = "", persistenceName = "test_table")
  private record ErrorFeed_MissingName(String id) {}

  @HttpFeed(path="/feed/test", feedName = "test", persistenceName = "")
  private record ErrorFeed_MissingPersistenceName(String id) {}



  @Test
  void defineFeed_should_accept_valid_feed() {
    final var feed = OkFeed.class.getAnnotation(HttpFeed.class);
    final var definition = registry.defineFeed(feed, serviceMock);
    Assertions.assertEquals("/feed/test/", definition.path());
    Assertions.assertEquals("test", definition.feed());
    Assertions.assertEquals("test_table", definition.table());
    Assertions.assertEquals(serviceMock, definition.feedItemService());
  }

  @Test
  void defineFeed_should_standardize_path() {
    final var feed = OkFeed_NonStandardPath.class.getAnnotation(HttpFeed.class);
    final var definition = registry.defineFeed(feed, serviceMock);
    Assertions.assertEquals("/feed/test/", definition.path());
  }

  @Test
  void defineFeed_should_reject_missing_path_prefix() {
    final var feed = ErrorFeed_MissingPathPrefix.class.getAnnotation(HttpFeed.class);
    assertThrows(IllegalArgumentException.class, () -> registry.defineFeed(feed, serviceMock));
  }

  @Test
  void defineFeed_should_reject_empty_path() {
    final var feed = ErrorFeed_EmptyPath.class.getAnnotation(HttpFeed.class);
    assertThrows(IllegalArgumentException.class, () -> registry.defineFeed(feed, serviceMock));
  }

  @Test
  void defineFeed_should_reject_missing_path() {
    final var feed = ErrorFeed_MissingPath.class.getAnnotation(HttpFeed.class);
    assertThrows(IllegalArgumentException.class, () -> registry.defineFeed(feed, serviceMock));
  }

  @Test
  void defineFeed_should_reject_missing_name() {
    final var feed = ErrorFeed_MissingName.class.getAnnotation(HttpFeed.class);
    assertThrows(IllegalArgumentException.class, () -> registry.defineFeed(feed, serviceMock));
  }

  @Test
  void defineFeed_should_reject_missing_persistence_name() {
    final var feed = ErrorFeed_MissingPersistenceName.class.getAnnotation(HttpFeed.class);
    assertThrows(IllegalArgumentException.class, () -> registry.defineFeed(feed, serviceMock));
  }



  @Test
  void getDefinedFeed_should_return_correct_feed() {
    final var feed = OkFeed.class.getAnnotation(HttpFeed.class);
    final var expected = registry.defineFeed(feed, serviceMock);

    final var read = registry.getDefinedFeed("/feed/test/");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());
  }

  @Test
  void getDefinedFeed_should_handle_nonstandard_path() {
    final var feed = OkFeed.class.getAnnotation(HttpFeed.class);
    final var expected = registry.defineFeed(feed, serviceMock);

    final var read = registry.getDefinedFeed("/feed/test");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());
  }

  @Test
  void getDefinedFeed_should_return_correct_feed_when_multiple_are_defined() {
    final var feed = OkFeed.class.getAnnotation(HttpFeed.class);
    final var expected = registry.defineFeed(feed, serviceMock);

    final var feed2 = OkFeed2.class.getAnnotation(HttpFeed.class);
    registry.defineFeed(feed2, serviceMock);

    final var read = registry.getDefinedFeed("/feed/test/");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());
  }

  @Test
  void getDefinedFeed_should_not_return_missing_definition() {
    final var feed = OkFeed.class.getAnnotation(HttpFeed.class);
    registry.defineFeed(feed, serviceMock);

    final var read = registry.getDefinedFeed("/feed/missing/");
    assertFalse(read.isPresent());
  }
}
