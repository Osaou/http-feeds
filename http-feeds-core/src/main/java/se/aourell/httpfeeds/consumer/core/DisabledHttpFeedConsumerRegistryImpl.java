package se.aourell.httpfeeds.consumer.core;

import se.aourell.httpfeeds.consumer.core.processing.EventFeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.spi.HttpFeedConsumerRegistry;

public class DisabledHttpFeedConsumerRegistryImpl implements HttpFeedConsumerRegistry {

  @Override
  public EventFeedConsumerProcessor defineHttpFeedConsumer(String feedName, String baseUri, Object bean) {
    return null;
  }

  @Override
  public void batchPollAndProcessHttpFeedEvents() {
    throw new UnsupportedOperationException(DisabledHttpFeedConsumerRegistryImpl.class.getName() + " does not support processing events");
  }
}
