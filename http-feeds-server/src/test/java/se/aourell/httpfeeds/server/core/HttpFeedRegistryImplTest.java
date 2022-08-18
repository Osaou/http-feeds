package se.aourell.httpfeeds.server.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.aourell.httpfeeds.server.api.HttpFeed;
import se.aourell.httpfeeds.server.spi.FeedItemService;
import se.aourell.httpfeeds.server.spi.HttpFeedRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpFeedRegistryImplTest {

  private HttpFeedRegistry registry;

  @BeforeEach
  void beforeEach() {
    registry = new HttpFeedRegistryImpl();
  }

  final FeedItemService serviceMock = new FeedItemService() {
    @Override
    public List<FeedItem> fetch(String lastEventId) {
      return null;
    }
    @Override
    public List<FeedItem> fetchWithTimeout(String lastEventId, Long timeoutMillis) {
      return null;
    }
  };

  @HttpFeed(path="/feed/test/", persistenceName = "test_table")
  private record OkFeed(String id) {}

  @HttpFeed(path="/feed/two/", persistenceName = "two_table")
  private record OkFeed2(String id) {}

  @HttpFeed(path="/feed/test", persistenceName = "test_table")
  private record OkFeed_NonStandardPath(String id) {}

  @HttpFeed(path="/test", persistenceName = "test_table")
  private record ErrorFeed_MissingPathPrefix(String id) {}

  @HttpFeed(path="/", persistenceName = "test_table")
  private record ErrorFeed_EmptyPath(String id) {}

  @HttpFeed(path="", persistenceName = "test_table")
  private record ErrorFeed_MissingPath(String id) {}



  @Test
  void defineFeed_should_accept_valid_feed() {
    final var feed = OkFeed.class.getAnnotation(HttpFeed.class);
    final var path = registry.validateFeedPath(feed.path());
    final var definition = registry.defineFeed(path, serviceMock);
    Assertions.assertEquals("/feed/test/", definition.path());
    Assertions.assertEquals(serviceMock, definition.feedItemService());
  }

  @Test
  void defineFeed_should_standardize_path() {
    final var feed = OkFeed_NonStandardPath.class.getAnnotation(HttpFeed.class);
    final var path = registry.validateFeedPath(feed.path());
    final var definition = registry.defineFeed(path, serviceMock);
    Assertions.assertEquals("/feed/test/", definition.path());
  }

  @Test
  void defineFeed_should_reject_missing_path_prefix() {
    final var feed = ErrorFeed_MissingPathPrefix.class.getAnnotation(HttpFeed.class);
    assertThrows(IllegalArgumentException.class, () -> registry.validateFeedPath(feed.path()));
  }

  @Test
  void defineFeed_should_reject_empty_path() {
    final var feed = ErrorFeed_EmptyPath.class.getAnnotation(HttpFeed.class);
    assertThrows(IllegalArgumentException.class, () -> registry.validateFeedPath(feed.path()));
  }

  @Test
  void defineFeed_should_reject_missing_path() {
    final var feed = ErrorFeed_MissingPath.class.getAnnotation(HttpFeed.class);
    assertThrows(IllegalArgumentException.class, () -> registry.validateFeedPath(feed.path()));
  }



  @Test
  void getDefinedFeed_should_return_correct_feed() {
    final var feed = OkFeed.class.getAnnotation(HttpFeed.class);
    final var path = registry.validateFeedPath(feed.path());
    final var expected = registry.defineFeed(path, serviceMock);

    final var read = registry.getDefinedFeed("/feed/test/");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());
  }

  @Test
  void getDefinedFeed_should_handle_nonstandard_path() {
    final var feed = OkFeed.class.getAnnotation(HttpFeed.class);
    final var path = registry.validateFeedPath(feed.path());
    final var expected = registry.defineFeed(path, serviceMock);

    final var read = registry.getDefinedFeed("/feed/test");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());
  }

  @Test
  void getDefinedFeed_should_return_correct_feed_when_multiple_are_defined() {
    final var feed = OkFeed.class.getAnnotation(HttpFeed.class);
    final var path = registry.validateFeedPath(feed.path());
    final var expected = registry.defineFeed(path, serviceMock);

    final var feed2 = OkFeed2.class.getAnnotation(HttpFeed.class);
    final var path2 = registry.validateFeedPath(feed2.path());
    registry.defineFeed(path2, serviceMock);

    final var read = registry.getDefinedFeed("/feed/test/");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());
  }

  @Test
  void getDefinedFeed_should_not_return_missing_definition() {
    final var feed = OkFeed.class.getAnnotation(HttpFeed.class);
    final var path = registry.validateFeedPath(feed.path());
    registry.defineFeed(path, serviceMock);

    final var read = registry.getDefinedFeed("/feed/missing/");
    assertFalse(read.isPresent());
  }
}
