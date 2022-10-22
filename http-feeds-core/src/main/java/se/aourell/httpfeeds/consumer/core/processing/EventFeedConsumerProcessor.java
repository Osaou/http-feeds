package se.aourell.httpfeeds.consumer.core.processing;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class EventFeedConsumerProcessor {

  private final String feedName;
  private final Object bean;
  private final Map<String, EventHandlerDefinition> eventHandlers;

  protected EventFeedConsumerProcessor(String feedName, Object bean) {
    this.feedName = feedName;
    this.bean = bean;
    this.eventHandlers = new HashMap<>();
  }

  public String getFeedName() {
    return feedName;
  }

  public Object getBean() {
    return bean;
  }

  public Optional<EventHandlerDefinition> findHandlerForEventType(String eventTypeName) {
    final var handler = eventHandlers.get(eventTypeName);
    return Optional.ofNullable(handler);
  }

  public void registerEventHandler(Class<?> eventType, Method handler) {
    final var callable = handler.getParameterTypes().length == 2
      ? new EventHandlerDefinition.ForEventAndMeta(handler, eventType)
      : new EventHandlerDefinition.ForEvent(handler, eventType);

    eventHandlers.put(eventType.getSimpleName(), callable);
  }
}
