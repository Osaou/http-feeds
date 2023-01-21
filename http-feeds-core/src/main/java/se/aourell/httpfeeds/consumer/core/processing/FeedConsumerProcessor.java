package se.aourell.httpfeeds.consumer.core.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.core.EventMetaData;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.util.Result;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class FeedConsumerProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(FeedConsumerProcessor.class);

  private final String feedConsumerName;
  private final String feedName;
  private final Object bean;
  private final String url;
  private final DomainEventDeserializer domainEventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;
  private final Map<String, EventHandlerDefinition> eventHandlers;

  private String lastProcessedId;

  public FeedConsumerProcessor(String feedConsumerName, Object bean, String feedName, String url, DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository) {
    this.feedConsumerName = feedConsumerName;
    this.bean = bean;
    this.feedName = feedName;
    this.url = url;
    this.domainEventDeserializer = domainEventDeserializer;
    this.feedConsumerRepository = feedConsumerRepository;

    this.eventHandlers = new HashMap<>();
    this.lastProcessedId = feedConsumerRepository.retrieveLastProcessedId(feedConsumerName)
      .orElse(null);
  }

  public String getFeedConsumerName() {
    return feedConsumerName;
  }

  public String getFeedName() {
    return feedName;
  }

  public Object getBean() {
    return bean;
  }

  public String getUrl() {
    return url;
  }

  public Optional<String> getLastProcessedId() {
    return Optional.ofNullable(lastProcessedId);
  }

  public void registerEventHandler(Class<?> eventType, Method handler) {
    final var callable = handler.getParameterTypes().length == 2
      ? new EventHandlerDefinition.ForEventAndMeta(handler, eventType)
      : new EventHandlerDefinition.ForEvent(handler, eventType);

    eventHandlers.put(eventType.getSimpleName(), callable);
  }

  public Result<Long> fetchAndProcessEvents(Function<FeedConsumerProcessor, Result<List<CloudEvent>>> fetch) {
    final Result<Long> updatedFailureCount = fetch.apply(this)
      .flatMap(events -> {
        try {
          for (final var event : events) {
            processEvent(event);
          }
        } catch (Exception e) {
          LOG.error("Exception when processing event", e);
          return Result.failure(e);
        }

        // reset failure count
        return Result.success(0L);
      });

    // persist id of last event we were able to process
    getLastProcessedId()
      .ifPresent(lastProcessedId -> feedConsumerRepository.storeLastProcessedId(feedConsumerName, lastProcessedId));

    return updatedFailureCount;
  }

  private void processEvent(CloudEvent event) throws Exception {
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

    lastProcessedId = Objects.requireNonNull(event.id());
  }

  public Optional<EventHandlerDefinition> findHandlerForEventType(String eventTypeName) {
    final var handler = eventHandlers.get(eventTypeName);
    return Optional.ofNullable(handler);
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
