package se.aourell.httpfeeds.client.core;

import java.lang.reflect.Method;

record EventHandlerWithEvent(Method method) implements EventHandlerCallable { }
