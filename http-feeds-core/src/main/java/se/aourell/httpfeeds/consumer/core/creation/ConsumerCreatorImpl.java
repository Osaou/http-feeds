package se.aourell.httpfeeds.consumer.core.creation;

import se.aourell.httpfeeds.consumer.api.ConsumerCreator;
import se.aourell.httpfeeds.consumer.core.EventMetaData;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ConsumerCreatorImpl implements ConsumerCreator {

  private final FeedConsumerProcessor feedConsumerProcessor;

  public ConsumerCreatorImpl(FeedConsumerProcessor feedConsumerProcessor) {
    this.feedConsumerProcessor = feedConsumerProcessor;
  }

  @Override
  public <EventType> ConsumerCreator registerEventHandler(Class<EventType> eventType, Consumer<EventType> handler) {
    feedConsumerProcessor.registerEventHandler(eventType, handler);
    return this;
  }

  @Override
  public <EventType> ConsumerCreator registerEventHandler(Class<EventType> eventType, BiConsumer<EventType, EventMetaData> handler) {
    feedConsumerProcessor.registerEventHandler(eventType, handler);
    return this;
  }
}
