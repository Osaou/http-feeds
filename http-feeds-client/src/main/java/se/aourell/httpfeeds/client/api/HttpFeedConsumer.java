package se.aourell.httpfeeds.client.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface HttpFeedConsumer {

  String name();
}
