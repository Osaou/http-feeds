package se.aourell.httpfeeds.producer.api;

import se.aourell.httpfeeds.producer.spi.EventBus;

import static se.aourell.httpfeeds.producer.spi.FeedItemRepository.DEFAULT_TABLE_NAME;

public interface EventFeedCreator {

  <EventBaseType> EventBus<EventBaseType> createEventFeed(String feedName, String persistenceName, Class<EventBaseType> feedEventBaseType, FeedAvailability availability);

  default <EventBaseType> EventBus<EventBaseType> createEventFeed(String feedName, Class<EventBaseType> feedEventBaseType, FeedAvailability availability) {
    return createEventFeed(feedName, DEFAULT_TABLE_NAME, feedEventBaseType, availability);
  }
}
