package se.aourell.httpfeeds.consumer.spi;

import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;

public interface LocalFeedConsumerRegistry {

  FeedConsumerProcessor defineLocalConsumer(String feedConsumerName, Object bean, String feedName);

  void batchProcessLocalFeedEvents();
}
