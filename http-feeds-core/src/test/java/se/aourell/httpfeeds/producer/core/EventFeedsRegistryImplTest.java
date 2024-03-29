package se.aourell.httpfeeds.producer.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.aourell.httpfeeds.producer.api.EventFeed;

class EventFeedsRegistryImplTest {

  @EventFeed(value ="test", persistenceName = "test_table")
  private record OkFeed(String id) { }

  @EventFeed(value ="other", persistenceName = "other_table")
  private record OkFeed2(String id) { }



  @Test
  void defineFeed_should_accept_valid_feed() {
    /*final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    final var definition = registry.defineFeed(serviceMock);
    Assertions.assertEquals("test", definition.name());
    Assertions.assertEquals("/feed/test", definition.feedPath());
    Assertions.assertEquals(serviceMock, definition.eventFeedService());
    Assertions.assertFalse(definition.publishedHttpFeed());*/
  }



  @Test
  void getDefinedFeed_should_return_correct_feed() {
    /*final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    final var expected = registry.defineFeed(path, serviceMock, true);

    final var read = registry.getPublishedHttpFeedByPath("/feed/test/");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());*/
  }

  @Test
  void getDefinedFeed_should_handle_nonstandard_path() {
    /*final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    final var expected = registry.defineFeed(path, serviceMock, true);

    final var read = registry.getPublishedHttpFeedByPath("/feed/test");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());*/
  }

  @Test
  void getDefinedFeed_should_return_correct_feed_when_multiple_are_defined() {
    /*final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    final var expected = registry.defineFeed(path, serviceMock, true);

    final var feed2 = OkFeed2.class.getAnnotation(EventFeed.class);
    final var path2 = feed2.value();
    registry.defineFeed(path2, serviceMock, true);

    final var read = registry.getPublishedHttpFeedByPath("/feed/test/");
    assertTrue(read.isPresent());
    Assertions.assertEquals(expected, read.get());*/
  }

  @Test
  void getDefinedFeed_should_not_return_missing_definition() {
    /*final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    registry.defineFeed(path, serviceMock, true);

    final var read = registry.getPublishedHttpFeedByPath("/feed/missing/");
    assertFalse(read.isPresent());*/
  }

  @Test
  void getDefinedFeed_should_not_return_unpublished_definition() {
    /*final var feed = OkFeed.class.getAnnotation(EventFeed.class);
    final var path = feed.value();
    registry.defineFeed(path, serviceMock, false);

    final var read = registry.getPublishedHttpFeedByPath(path);
    assertFalse(read.isPresent());*/
  }
}
