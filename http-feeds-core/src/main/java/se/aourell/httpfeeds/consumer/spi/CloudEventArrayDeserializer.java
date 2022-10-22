package se.aourell.httpfeeds.consumer.spi;

import se.aourell.httpfeeds.CloudEvent;

public interface CloudEventArrayDeserializer {

  CloudEvent[] toCloudEvents(String string);
}
