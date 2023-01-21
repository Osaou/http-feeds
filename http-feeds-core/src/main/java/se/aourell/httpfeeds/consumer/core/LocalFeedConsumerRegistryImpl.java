package se.aourell.httpfeeds.consumer.core;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessorGroup;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.LocalFeedConsumerRegistry;
import se.aourell.httpfeeds.producer.core.CloudEventMapper;
import se.aourell.httpfeeds.producer.core.EventFeedDefinition;
import se.aourell.httpfeeds.producer.core.FeedItem;
import se.aourell.httpfeeds.producer.spi.EventFeedRegistry;
import se.aourell.httpfeeds.producer.spi.FeedItemService;
import se.aourell.httpfeeds.util.Result;

import java.util.List;

public class LocalFeedConsumerRegistryImpl implements LocalFeedConsumerRegistry {

  private final FeedConsumerProcessorGroup feedConsumerProcessorGroup;
  private final EventFeedRegistry eventFeedRegistry;
  private final CloudEventMapper cloudEventMapper;

  public LocalFeedConsumerRegistryImpl(DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository, EventFeedRegistry eventFeedRegistry, CloudEventMapper cloudEventMapper) {
    this.feedConsumerProcessorGroup = new FeedConsumerProcessorGroup(domainEventDeserializer, feedConsumerRepository);
    this.eventFeedRegistry = eventFeedRegistry;
    this.cloudEventMapper = cloudEventMapper;
  }

  @Override
  public FeedConsumerProcessor defineLocalConsumer(String feedConsumerName, Object bean, String feedName) {
    return feedConsumerProcessorGroup.defineFeedConsumer(feedConsumerName, bean, feedName, null);
  }

  @Override
  public void batchProcessLocalFeedEvents() {
    feedConsumerProcessorGroup.batchFetchAndProcessEvents(processor -> {
      try {
        final FeedItemService feedItemService = eventFeedRegistry.getLocalFeedByName(processor.getFeedName())
          .map(EventFeedDefinition::feedItemService)
          .orElseThrow(() -> new RuntimeException("Unable to find defined feed item service by name: " + processor.getFeedName()));

        final List<FeedItem> feedItems = processor.getLastProcessedId()
          .map(lastProcessedId -> feedItemService.fetch(lastProcessedId, null))
          .orElseGet(() -> feedItemService.fetch(null, null));

        final List<CloudEvent> result = feedItems.stream()
          .map(cloudEventMapper::mapFeedItem)
          .toList();

        return Result.success(result);
      } catch (Exception e) {
        return Result.failure(e);
      }
    });
  }
}
