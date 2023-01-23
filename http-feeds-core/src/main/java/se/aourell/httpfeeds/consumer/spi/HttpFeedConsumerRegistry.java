package se.aourell.httpfeeds.consumer.spi;

import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;

public interface HttpFeedConsumerRegistry {

  FeedConsumerProcessor defineHttpFeedConsumer(String feedConsumerName, Object bean, String feedName, String feedUrl);

  void batchPollAndProcessHttpFeedEvents();
}
