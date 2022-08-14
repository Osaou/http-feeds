package se.aourell.httpfeeds.client.spi;

import se.aourell.httpfeeds.client.core.FeedConsumerDefinition;

public interface FeedConsumerProcessor {

  FeedConsumerDefinition defineConsumer(String feedName, String url, Object bean);

  void batchPollAndProcessEvents();
}
