package se.aourell.httpfeeds.client.core;

import java.lang.reflect.Method;
import java.util.function.Supplier;

sealed abstract class EventHandlerDefinition
  permits EventHandlerDefinition.ForEvent, EventHandlerDefinition.ForEventAndMeta {

  private final Method method;

  protected EventHandlerDefinition(Method method) {
    this.method = method;
  }

  protected Method method() {
    return method;
  }

  abstract void invoke(Object bean, Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws Exception;



  static final class ForEvent extends EventHandlerDefinition {
    ForEvent(Method method) {
      super(method);
    }

    @Override
    void invoke(Object bean, Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws Exception {
      method().invoke(bean, deserializedData);
    }
  }



  static final class ForEventAndMeta extends EventHandlerDefinition {
    ForEventAndMeta(Method method) {
      super(method);
    }

    @Override
    void invoke(Object bean, Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws Exception {
      final var meta = metaDataSupplier.get();
      method().invoke(bean, deserializedData, meta);
    }
  }
}
