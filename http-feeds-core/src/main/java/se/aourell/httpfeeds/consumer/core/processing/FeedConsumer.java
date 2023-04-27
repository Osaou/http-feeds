package se.aourell.httpfeeds.consumer.core.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.core.EventMetaData;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;
import se.aourell.httpfeeds.tracing.core.DeadLetterQueueService;
import se.aourell.httpfeeds.tracing.spi.ApplicationShutdownDetector;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.util.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FeedConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(FeedConsumer.class);

  private static final int MAX_RETRIES_BEFORE_SHELVING_IN_DLQ = 2;

  private final String feedConsumerName;
  private final String feedName;
  private final String url;
  private final LocalFeedFetcher localFeedFetcher;
  private final RemoteFeedFetcher remoteFeedFetcher;
  private final DomainEventDeserializer domainEventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;
  private final ApplicationShutdownDetector applicationShutdownDetector;
  private final DeadLetterQueueService deadLetterQueueService;
  private final DeadLetterQueueRepository deadLetterQueueRepository;
  private final Map<String, EventHandlerDefinition> eventHandlers;

  private int processingFailureCount;
  private String lastProcessedId;
  private String updatedLastProcessedId;
  private boolean isProcessingDlq;

  public FeedConsumer(String feedConsumerName,
                      String feedName,
                      String url,
                      LocalFeedFetcher localFeedFetcher,
                      RemoteFeedFetcher remoteFeedFetcher,
                      DomainEventDeserializer domainEventDeserializer,
                      FeedConsumerRepository feedConsumerRepository,
                      ApplicationShutdownDetector applicationShutdownDetector,
                      DeadLetterQueueService deadLetterQueueService,
                      DeadLetterQueueRepository deadLetterQueueRepository) {
    this.feedConsumerName = feedConsumerName;
    this.feedName = feedName;
    this.url = url;

    this.localFeedFetcher = localFeedFetcher;
    this.remoteFeedFetcher = remoteFeedFetcher;
    this.domainEventDeserializer = domainEventDeserializer;
    this.feedConsumerRepository = feedConsumerRepository;
    this.applicationShutdownDetector = applicationShutdownDetector;
    this.deadLetterQueueService = deadLetterQueueService;
    this.deadLetterQueueRepository = deadLetterQueueRepository;

    this.processingFailureCount = 0;
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

    // first work off any re-introduced items from the DLQ
    final List<CloudEvent> reintroducedFromDlq = deadLetterQueueRepository.findReintroduced(feedConsumerName);
    if (!reintroducedFromDlq.isEmpty()) {
      isProcessingDlq = true;
      return Result.success(reintroducedFromDlq);
    }

    isProcessingDlq = false;
    return url == null
      ? localFeedFetcher.fetchLocalEvents(this)
      : remoteFeedFetcher.fetchRemoteEvents(this);
  }

  public Result<Boolean> processEvent(CloudEvent event) {
    final String eventId = event.id();
    final String traceId = event.traceId()
      .orElse(eventId);

    final boolean handled;
    try {
      final String eventTypeName = event.type();
      final Optional<EventHandlerDefinition> eventHandler = findHandlerForEventType(eventTypeName);

      if (eventHandler.isPresent()) {
        // should we shelve this event on DLQ by association?
        if (!isProcessingDlq && event.traceId().isPresent() && deadLetterQueueRepository.isTraceShelved(traceId)) {
          try {
            deadLetterQueueRepository.addEventToShelvedTrace(traceId, event);
            LOG.warn("DLQ: Shelved event with ID {} because of matched trace {} being shelved", eventId, traceId);
          } catch (Throwable e) {
            if (!applicationShutdownDetector.isGracefulShutdown()) {
              LOG.error("DLQ: Unable to operate on dead-letter queue", e);
            }

            return Result.failure(e);
          }

          updatedLastProcessedId = eventId;
          processingFailureCount = 0;
          return Result.success();
        }

        handled = true;
        final Class<?> eventType = eventHandler.get().eventType();

        final Object deserializedData;
        if (CloudEvent.DELETE_METHOD.equals(event.method())) {
          deserializedData = eventType.getConstructor(String.class).newInstance(event.subject());
        } else {
          final Object data = event.data();
          deserializedData = domainEventDeserializer.toDomainEvent(data, eventType);
        }

        eventHandler.get().invoke(deserializedData, () -> createEventMetaData(event));
      } else {
        handled = false;
      }
    } catch (Throwable e) {
      if (applicationShutdownDetector.isGracefulShutdown()) {
        return Result.failure(e);
      }

      ++processingFailureCount;
      LOG.warn("DLQ: Failure count is now " + processingFailureCount);

      try {
        if (isProcessingDlq) {
          deadLetterQueueRepository.keepShelved(traceId);
          LOG.warn("DLQ: Re-Shelved all remaining events in trace {}, starting with event ID {}, because of failed processing attempt", traceId, eventId);

          processingFailureCount = 0;
          return Result.failure();
        }

        // check if it's time to shelve this event in the dead-letter queue
        if (processingFailureCount >= MAX_RETRIES_BEFORE_SHELVING_IN_DLQ) {
          deadLetterQueueService.shelveFromFeed(event, feedConsumerName, e);
          LOG.warn("DLQ: Shelved event with ID {} because of too many failed processing attempts", eventId);

          updatedLastProcessedId = eventId;
          processingFailureCount = 0;
          return Result.success();
        }
      } catch (Throwable e2) {
        if (!applicationShutdownDetector.isGracefulShutdown()) {
          LOG.error("DLQ: Unable to operate on dead-letter queue", e2);
        }
      }

      return Result.failure(e);
    }

    if (handled) {
      processingFailureCount = 0;

      if (isProcessingDlq) {
        try {
          deadLetterQueueRepository.markDelivered(traceId, eventId);
          LOG.warn("DLQ: Successful processing of event with ID {}", eventId);
        } catch (Throwable e) {
          if (!applicationShutdownDetector.isGracefulShutdown()) {
            LOG.error("DLQ: Unable to operate on dead-letter queue", e);
          }

          return Result.failure(e);
        }
      } else {
        updatedLastProcessedId = eventId;
      }
    }

    return Result.success();
  }

  public Optional<EventHandlerDefinition> findHandlerForEventType(String eventTypeName) {
    final EventHandlerDefinition handler = eventHandlers.get(eventTypeName);
    return Optional.ofNullable(handler);
  }

  private EventMetaData createEventMetaData(CloudEvent cloudEvent) {
    return new EventMetaData(
      cloudEvent.id(),
      cloudEvent.traceId().orElseGet(cloudEvent::id),
      cloudEvent.subject(),
      cloudEvent.time(),
      cloudEvent.source()
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