package se.aourell.httpfeeds.consumer.core;

import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.spi.HttpFeedConsumerRegistry;

public class DisabledHttpFeedConsumerRegistryImpl implements HttpFeedConsumerRegistry {

  @Override
  public FeedConsumerProcessor defineHttpFeedConsumer(String feedConsumerName, Object bean, String feedName, String feedUrl) {
    return null;
  }

  @Override
  public void batchPollAndProcessHttpFeedEvents() {
    throw new UnsupportedOperationException(DisabledHttpFeedConsumerRegistryImpl.class.getName() + " does not support processing events");
  }
}
