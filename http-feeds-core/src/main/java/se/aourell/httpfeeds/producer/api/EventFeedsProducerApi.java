package se.aourell.httpfeeds.producer.api;

import se.aourell.httpfeeds.producer.spi.EventBus;

import static se.aourell.httpfeeds.producer.spi.FeedItemRepository.DEFAULT_TABLE_NAME;

public interface EventFeedsProducerApi {

  default
  <EventBaseType> EventBus<EventBaseType> publishEventFeed(String feedName, Class<EventBaseType> feedEventBaseType, FeedAvailability availability) {
    return publishEventFeed(feedName, feedEventBaseType, availability, DEFAULT_TABLE_NAME);
  }

  <EventBaseType> EventBus<EventBaseType> publishEventFeed(String feedName, Class<EventBaseType> feedEventBaseType, FeedAvailability availability, String persistenceName);
}
