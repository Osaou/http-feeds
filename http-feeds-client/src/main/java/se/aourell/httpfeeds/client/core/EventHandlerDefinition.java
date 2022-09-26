package se.aourell.httpfeeds.client.core;

import java.lang.reflect.Method;
import java.util.function.Supplier;

abstract sealed class EventHandlerDefinition
  permits EventHandlerDefinition.ForEvent, EventHandlerDefinition.ForEventAndMeta {

  private final Method method;
  private final Class<?> eventType;

  protected EventHandlerDefinition(Method method, Class<?> eventType) {
    this.method = method;
    this.eventType = eventType;
  }

  protected Method method() {
    return method;
  }

  Class<?> eventType() {
    return eventType;
  }

  abstract void invoke(Object bean, Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws Exception;


  static final class ForEvent extends EventHandlerDefinition {
    ForEvent(Method method, Class<?> eventType) {
      super(method, eventType);
    }

    @Override
    void invoke(Object bean, Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws Exception {
      method().invoke(bean, deserializedData);
    }
  }



  static final class ForEventAndMeta extends EventHandlerDefinition {
    ForEventAndMeta(Method method, Class<?> eventType) {
      super(method, eventType);
    }

    @Override
    void invoke(Object bean, Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws Exception {
      final var meta = metaDataSupplier.get();
      method().invoke(bean, deserializedData, meta);
    }
  }
}
