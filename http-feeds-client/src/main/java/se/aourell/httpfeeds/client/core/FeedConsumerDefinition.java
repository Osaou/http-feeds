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

  public String getFeedName() {
    return feedName;
  }

  public Object getBean() {
    return bean;
  }

  public String getUrl() {
    return url;
  }

  public Optional<String> getLastProcessedId() {
    return Optional.ofNullable(lastProcessedId);
  }

  public void registerEventHandler(Class<?> eventType, Method handler) {
    eventHandlers.put(eventType.getName(), handler);
  }

  public void processEvent(CloudEvent event) throws Throwable {
    final var eventTypeName = event.type();

    if (eventHandlers.containsKey(eventTypeName)) {
      final var handler = eventHandlers.get(eventTypeName);
      final var eventType = Class.forName(eventTypeName);

      final Object deserializedData;
      if (CloudEvent.DELETE_METHOD.equals(event.method())) {
        deserializedData = eventType.getConstructor(String.class).newInstance(event.id());
      } else {
        final var data = event.data();
        deserializedData = eventDeserializer.toDomainEvent(data, eventType);
      }

      handler.invoke(bean, deserializedData);
    }

    lastProcessedId = event.id();
  }
}
