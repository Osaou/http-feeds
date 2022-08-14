package se.aourell.httpfeeds.client.spi;

import se.aourell.httpfeeds.core.CloudEvent;

public interface CloudEventArrayDeserializer {

  CloudEvent[] toCloudEvents(String string);
}
