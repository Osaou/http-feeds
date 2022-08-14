package se.aourell.httpfeeds.client.core;

import se.aourell.httpfeeds.client.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.core.CloudEvent;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FeedConsumerDefinition {

  private final String feedName;
  private final String url;
  private final Object bean;
  private final Map<String, EventHandlerCallable> eventHandlers = new HashMap<>();
  private final DomainEventDeserializer domainEventDeserializer;

  private String lastProcessedId;

  FeedConsumerDefinition(String feedName, String url, Object bean, DomainEventDeserializer domainEventDeserializer, String lastProcessedId) {
    this.feedName = feedName;
    this.bean = bean;
    this.url = url;
    this.domainEventDeserializer = domainEventDeserializer;
    this.lastProcessedId = lastProcessedId;
  }

  public Object getBean() {
    return bean;
  }

  public void registerEventHandler(Class<?> eventType, Method handler) {
    final var callable = handler.getParameterTypes().length == 2
      ? new EventHandlerWithEventAndMeta(handler)
      : new EventHandlerWithEvent(handler);
    eventHandlers.put(eventType.getName(), callable);
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

  private EventMetaData createEventMetaData(CloudEvent currentCloudEvent) {
    return new EventMetaData(
      currentCloudEvent.specversion(),
      currentCloudEvent.id(),
      currentCloudEvent.source(),
      currentCloudEvent.time()
    );
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
        deserializedData = domainEventDeserializer.toDomainEvent(data, eventType);
      }

      if (handler instanceof EventHandlerWithEventAndMeta h) {
        h.method().invoke(bean, deserializedData, createEventMetaData(event));
      } else if (handler instanceof EventHandlerWithEvent h) {
        h.method().invoke(bean, deserializedData);
      }
    }

    lastProcessedId = event.id();
  }
}
