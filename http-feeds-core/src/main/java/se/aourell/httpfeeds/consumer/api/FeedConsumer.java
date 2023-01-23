package se.aourell.httpfeeds.consumer.api;

import se.aourell.httpfeeds.consumer.core.EventMetaData;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface FeedConsumer {

  <EventType> FeedConsumer registerHandler(Class<EventType> eventType, Consumer<EventType> handler);
  <EventType> FeedConsumer registerHandler(Class<EventType> eventType, BiConsumer<EventType, EventMetaData> handler);
}
