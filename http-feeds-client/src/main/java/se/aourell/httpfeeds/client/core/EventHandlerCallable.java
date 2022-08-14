package se.aourell.httpfeeds.client.core;

sealed interface EventHandlerCallable permits EventHandlerWithEvent, EventHandlerWithEventAndMeta { }
