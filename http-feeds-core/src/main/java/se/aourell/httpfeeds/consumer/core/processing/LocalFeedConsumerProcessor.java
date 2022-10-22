package se.aourell.httpfeeds.consumer.core.processing;

import se.aourell.httpfeeds.consumer.core.EventMetaData;

import java.time.Instant;
import java.time.ZoneOffset;

public class LocalFeedConsumerProcessor extends EventFeedConsumerProcessor {

  public LocalFeedConsumerProcessor(String feedName, Object bean) {
    super(feedName, bean);
  }

  public void processEvent(String id, String subject, Object event, Instant time) throws Exception {
    final var eventTypeName = event.getClass().getSimpleName();
    final var eventHandler = findHandlerForEventType(eventTypeName);
    if (eventHandler.isPresent()) {
      eventHandler.get().invoke(getBean(), event, () -> createEventMetaData(id, subject, time));
    }
  }

  private EventMetaData createEventMetaData(String id, String subject, Instant time) {
    return new EventMetaData(
      id,
      subject,
      time.atOffset(ZoneOffset.UTC),
      getFeedName()
    );
  }
}
