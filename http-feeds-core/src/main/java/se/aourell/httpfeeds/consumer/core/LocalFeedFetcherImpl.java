package se.aourell.httpfeeds.consumer.core;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.producer.core.CloudEventMapper;
import se.aourell.httpfeeds.producer.core.EventFeedDefinition;
import se.aourell.httpfeeds.producer.core.FeedItem;
import se.aourell.httpfeeds.producer.spi.EventFeedRegistry;
import se.aourell.httpfeeds.producer.spi.FeedItemService;
import se.aourell.httpfeeds.util.Result;

import java.util.List;

public class LocalFeedFetcherImpl implements LocalFeedFetcher {

  private final EventFeedRegistry eventFeedRegistry;
  private final CloudEventMapper cloudEventMapper;

  public LocalFeedFetcherImpl(EventFeedRegistry eventFeedRegistry, CloudEventMapper cloudEventMapper) {
    this.eventFeedRegistry = eventFeedRegistry;
    this.cloudEventMapper = cloudEventMapper;
  }

  @Override
  public Result<List<CloudEvent>> fetchLocalEvents(FeedConsumerProcessor feedConsumerProcessor) {
    try {
      final FeedItemService feedItemService = eventFeedRegistry.getLocalFeedByName(feedConsumerProcessor.getFeedName())
        .map(EventFeedDefinition::feedItemService)
        .orElseThrow(() -> new RuntimeException("Unable to find defined feed item service by name: " + feedConsumerProcessor.getFeedName()));

      final List<FeedItem> feedItems = feedConsumerProcessor.getLastProcessedId()
        .map(lastProcessedId -> feedItemService.fetch(lastProcessedId, null))
        .orElseGet(() -> feedItemService.fetch(null, null));

      final List<CloudEvent> result = feedItems.stream()
        .map(cloudEventMapper::mapFeedItem)
        .toList();

      return Result.success(result);
    } catch (Exception e) {
      return Result.failure(e);
    }
  }
}
