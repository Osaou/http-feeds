package se.aourell.httpfeeds.consumer.core.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.core.EventMetaData;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;
import se.aourell.httpfeeds.util.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FeedConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(FeedConsumer.class);

  private final String feedConsumerName;
  private final String feedName;
  private final String url;
  private final LocalFeedFetcher localFeedFetcher;
  private final RemoteFeedFetcher remoteFeedFetcher;
  private final DomainEventDeserializer domainEventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;
  private final Map<String, EventHandlerDefinition> eventHandlers;

  private String lastProcessedId;
  private String updatedLastProcessedId;

  public FeedConsumer(String feedConsumerName,
                      String feedName,
                      String url,
                      LocalFeedFetcher localFeedFetcher,
                      RemoteFeedFetcher remoteFeedFetcher,
                      DomainEventDeserializer domainEventDeserializer,
                      FeedConsumerRepository feedConsumerRepository) {
    this.feedConsumerName = feedConsumerName;
    this.feedName = feedName;
    this.url = url;

    this.localFeedFetcher = localFeedFetcher;
    this.remoteFeedFetcher = remoteFeedFetcher;
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

  public String getUrl() {
    return url;
  }

  public Optional<String> getLastProcessedId() {
    return Optional.ofNullable(lastProcessedId);
  }

  public <EventType> void registerEventHandler(Class<EventType> eventType, Consumer<EventType> handler) {
    final EventHandlerDefinition callable = new EventHandlerDefinition.RegisteredForEvent<>(eventType, handler);
    eventHandlers.put(eventType.getSimpleName(), callable);
  }

  public <EventType> void registerEventHandler(Class<EventType> eventType, BiConsumer<EventType, EventMetaData> handler) {
    final EventHandlerDefinition callable = new EventHandlerDefinition.RegisteredForEventAndMeta<>(eventType, handler);
    eventHandlers.put(eventType.getSimpleName(), callable);
  }

  public Result<List<CloudEvent>> fetchEvents() {
    updatedLastProcessedId = lastProcessedId;

    return url == null
      ? localFeedFetcher.fetchLocalEvents(this)
      : remoteFeedFetcher.fetchRemoteEvents(this);
  }

  public Result<Boolean> processEvent(CloudEvent event) {
    try {
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

        eventHandler.get().invoke(deserializedData, () -> createEventMetaData(event));
      }

      updatedLastProcessedId = event.id();

      return Result.success();
    } catch (Throwable e) {
      return Result.failure(e);
    }
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

  public void persistProgress() {
    // persist id of last event we were able to process, if any
    if (updatedLastProcessedId != null && !updatedLastProcessedId.equals(lastProcessedId)) {
      feedConsumerRepository.storeLastProcessedId(feedConsumerName, updatedLastProcessedId);
      lastProcessedId = updatedLastProcessedId;
    }
  }
}
