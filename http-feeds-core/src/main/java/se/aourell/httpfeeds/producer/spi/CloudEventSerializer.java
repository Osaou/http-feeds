package se.aourell.httpfeeds.producer.spi;

import se.aourell.httpfeeds.CloudEvent;

import java.util.List;

public interface CloudEventSerializer {

  String toString(CloudEvent event);

  String toString(List<CloudEvent> events);
}
