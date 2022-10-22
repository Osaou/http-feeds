package se.aourell.httpfeeds.consumer.spi;

import se.aourell.httpfeeds.consumer.core.processing.EventFeedConsumerProcessor;

import java.time.Instant;

public interface LocalFeedConsumerRegistry {

  EventFeedConsumerProcessor defineLocalConsumer(String feedName, Object bean);

  void processLocalEvent(String id, String subject, Object event, Instant time);
}
