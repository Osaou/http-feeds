package se.aourell.httpfeeds.client.core;

import java.lang.reflect.Method;

sealed interface EventHandlerDefinition permits EventHandlerDefinition.WithEvent, EventHandlerDefinition.WithEventAndMeta {

  record WithEvent(Method method) implements EventHandlerDefinition { }
  record WithEventAndMeta(Method method) implements EventHandlerDefinition { }
}
