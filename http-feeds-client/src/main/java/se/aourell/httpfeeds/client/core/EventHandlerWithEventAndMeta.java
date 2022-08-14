package se.aourell.httpfeeds.client.core;

import java.lang.reflect.Method;

record EventHandlerWithEventAndMeta(Method method) implements EventHandlerCallable { }
