package se.aourell.httpfeeds.consumer.spi;

import se.aourell.httpfeeds.consumer.core.processing.EventFeedConsumerProcessor;

public interface HttpFeedConsumerRegistry {

  EventFeedConsumerProcessor defineHttpFeedConsumer(String feedName, String baseUri, Object bean);

  void batchPollAndProcessHttpFeedEvents();
}
