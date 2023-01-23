package se.aourell.httpfeeds.consumer.core.processing;

import se.aourell.httpfeeds.consumer.core.EventMetaData;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

abstract class EventHandlerDefinition {

  private final Class<?> eventType;

  protected EventHandlerDefinition(Class<?> eventType) {
    this.eventType = eventType;
  }

  public Class<?> eventType() {
    return eventType;
  }

  abstract void invoke(Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws Exception;



  static abstract class Annotated extends EventHandlerDefinition {
    protected final Object bean;
    protected final Method method;

    protected Annotated(Class<?> eventType, Object bean, Method method) {
      super(eventType);
      this.bean = bean;
      this.method = method;
    }
  }

  static class AnnotatedForEvent extends Annotated {
    AnnotatedForEvent(Class<?> eventType, Object bean, Method method) {
      super(eventType, bean, method);
    }

    @Override
    void invoke(Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws Exception {
      method.invoke(bean, deserializedData);
    }
  }

  static class AnnotatedForEventAndMeta extends Annotated {
    AnnotatedForEventAndMeta(Class<?> eventType, Object bean, Method method) {
      super(eventType, bean, method);
    }

    @Override
    void invoke(Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws Exception {
      final var meta = metaDataSupplier.get();
      method.invoke(bean, deserializedData, meta);
    }
  }



  static abstract class Registered extends EventHandlerDefinition {
    protected Registered(Class<?> eventType) {
      super(eventType);
    }
  }

  static class RegisteredForEvent<EventType> extends Registered {
    protected Consumer<EventType> handler;

    RegisteredForEvent(Class<EventType> eventType, Consumer<EventType> handler) {
      super(eventType);
      this.handler = handler;
    }

    @Override
    void invoke(Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws ClassCastException {
      handler.accept((EventType) deserializedData);
    }
  }

  static class RegisteredForEventAndMeta<EventType> extends Registered {
    protected BiConsumer<EventType, EventMetaData> handler;

    RegisteredForEventAndMeta(Class<EventType> eventType, BiConsumer<EventType, EventMetaData> handler) {
      super(eventType);
      this.handler = handler;
    }

    @Override
    void invoke(Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws ClassCastException {
      final var meta = metaDataSupplier.get();
      handler.accept((EventType) deserializedData, meta);
    }
  }
}
