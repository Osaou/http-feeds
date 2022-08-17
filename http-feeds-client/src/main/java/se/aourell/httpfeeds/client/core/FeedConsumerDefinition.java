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
  private final String packageName;
  private final Map<String, EventHandlerDefinition> eventHandlers = new HashMap<>();
  private final DomainEventDeserializer domainEventDeserializer;

  private String lastProcessedId;

  FeedConsumerDefinition(String feedName, String url, Object bean, String packageName, DomainEventDeserializer domainEventDeserializer, String lastProcessedId) {
    this.feedName = feedName;
    this.url = url;
    this.bean = bean;
    this.packageName = packageName;
    this.domainEventDeserializer = domainEventDeserializer;
    this.lastProcessedId = lastProcessedId;
  }

  public Object getBean() {
    return bean;
  }

  public void registerEventHandler(Class<?> eventType, Method handler) {
    final var callable = handler.getParameterTypes().length == 2
      ? new EventHandlerDefinition.ForEventAndMeta(handler)
      : new EventHandlerDefinition.ForEvent(handler);
    eventHandlers.put(eventType.getSimpleName(), callable);
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
      currentCloudEvent.id(),
      currentCloudEvent.subject(),
      currentCloudEvent.time(),
      currentCloudEvent.source()
    );
  }

  void processEvent(CloudEvent event) throws Exception {
    final var eventTypeName = event.type();

    if (eventHandlers.containsKey(eventTypeName)) {
      final var eventTypeNameWithPackage = String.format("%s.%s", packageName, eventTypeName);
      final var eventType = Class.forName(eventTypeNameWithPackage);

      final Object deserializedData;
      if (CloudEvent.DELETE_METHOD.equals(event.method())) {
        deserializedData = eventType.getConstructor(String.class).newInstance(event.subject());
      } else {
        final var data = event.data();
        deserializedData = domainEventDeserializer.toDomainEvent(data, eventType);
      }

      final var eventHandler = eventHandlers.get(eventTypeName);
      eventHandler.invoke(bean, deserializedData, () -> createEventMetaData(event));
    }

    lastProcessedId = event.id();
  }
}
