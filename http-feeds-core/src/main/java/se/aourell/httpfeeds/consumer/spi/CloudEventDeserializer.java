package se.aourell.httpfeeds.consumer.spi;

import se.aourell.httpfeeds.CloudEvent;

public interface CloudEventDeserializer {

  CloudEvent toCloudEvent(String string);

  CloudEvent[] toCloudEvents(String string);
}
