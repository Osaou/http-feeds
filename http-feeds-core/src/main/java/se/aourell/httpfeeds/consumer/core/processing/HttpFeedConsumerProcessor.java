package se.aourell.httpfeeds.consumer.core.processing;

import se.aourell.httpfeeds.consumer.core.EventMetaData;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.CloudEvent;

import java.util.Optional;

public class HttpFeedConsumerProcessor extends EventFeedConsumerProcessor {

  private final String url;
  private final DomainEventDeserializer domainEventDeserializer;

  private String lastProcessedId;

  public HttpFeedConsumerProcessor(String feedName, Object bean, String url, DomainEventDeserializer domainEventDeserializer, String lastProcessedId) {
    super(feedName, bean);
    this.url = url;
    this.domainEventDeserializer = domainEventDeserializer;
    this.lastProcessedId = lastProcessedId;
  }

  public String getUrl() {
    return url;
  }

  public Optional<String> getLastProcessedId() {
    return Optional.ofNullable(lastProcessedId);
  }

  public void processEvent(CloudEvent event) throws Exception {
    final var eventTypeName = event.type();
    final var eventHandler = findHandlerForEventType(eventTypeName);

    if (eventHandler.isPresent()) {
      final var eventType = eventHandler.get().eventType();

      final Object deserializedData;
      if (CloudEvent.DELETE_METHOD.equals(event.method())) {
        deserializedData = eventType.getConstructor(String.class).newInstance(event.subject());
      } else {
        final var data = event.data();
        deserializedData = domainEventDeserializer.toDomainEvent(data, eventType);
      }

      eventHandler.get().invoke(getBean(), deserializedData, () -> createEventMetaData(event));
    }

    lastProcessedId = event.id();
  }

  private EventMetaData createEventMetaData(CloudEvent currentCloudEvent) {
    return new EventMetaData(
      currentCloudEvent.id(),
      currentCloudEvent.subject(),
      currentCloudEvent.time(),
      currentCloudEvent.source()
    );
  }
}
