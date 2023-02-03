package se.aourell.httpfeeds.consumer.core.processing;

import se.aourell.httpfeeds.consumer.core.EventMetaData;

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



  static class RegisteredForEvent<EventType> extends EventHandlerDefinition {
    private final Consumer<EventType> handler;

    RegisteredForEvent(Class<EventType> eventType, Consumer<EventType> handler) {
      super(eventType);
      this.handler = handler;
    }

    @Override
    void invoke(Object deserializedData, Supplier<EventMetaData> metaDataSupplier) throws ClassCastException {
      handler.accept((EventType) deserializedData);
    }
  }

  static class RegisteredForEventAndMeta<EventType> extends EventHandlerDefinition {
    private final BiConsumer<EventType, EventMetaData> handler;

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
