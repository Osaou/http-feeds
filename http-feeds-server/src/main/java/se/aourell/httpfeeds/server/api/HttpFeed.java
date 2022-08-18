package se.aourell.httpfeeds.server.api;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@IndexAnnotated
public @interface HttpFeed {

  String path();
  String persistenceName() default "";
}
