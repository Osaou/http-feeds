package se.aourell.httpfeeds.api;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@IndexAnnotated
public @interface HttpFeed {

  String feed() default "";
  String path() default "";
  String table() default "";
}
