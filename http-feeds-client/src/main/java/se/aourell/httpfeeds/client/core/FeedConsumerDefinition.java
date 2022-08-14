package se.aourell.httpfeeds.client.core;

import se.aourell.httpfeeds.client.spi.EventDeserializer;
import se.aourell.httpfeeds.core.CloudEvent;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FeedConsumerDefinition {

  private final String feedName;
  private final String url;
  private final Object bean;
  private final Map<String, Method> eventHandlers = new HashMap<>();
  private final EventDeserializer eventDeserializer;

  private String lastProcessedId;

  FeedConsumerDefinition(String feedName, String url, Object bean, EventDeserializer eventDeserializer, String lastProcessedId) {
    this.feedName = feedName;
    this.bean = bean;
    this.url = url;
    this.eventDeserializer = eventDeserializer;
    this.lastProcessedId = lastProcessedId;
  }

  public Object getBean() {
    return bean;
  }

  public void registerEventHandler(Class<?> eventType, Method handler) {
    eventHandlers.put(eventType.getName(), handler);
  }

  String getFeedName() {
    return feedName;
  }

  String getUrl() {
    return url;
  }

  Optional<String> getLastProcessedId() {
    return Optional.ofNullable(lastProcessedId);
  }

  void processEvent(CloudEvent event) throws Throwable {
    final var eventTypeName = event.type();

    if (eventHandlers.containsKey(eventTypeName)) {
      final var handler = eventHandlers.get(eventTypeName);
      final var eventType = Class.forName(eventTypeName);

      final Object deserializedData;
      if (CloudEvent.DELETE_METHOD.equals(event.method())) {
        deserializedData = eventType.getConstructor(String.class).newInstance(event.subject());
      } else {
        final var data = event.data();
        deserializedData = eventDeserializer.toDomainEvent(data, eventType);
      }

      handler.invoke(bean, deserializedData);
    }

    lastProcessedId = event.id();
  }
}
