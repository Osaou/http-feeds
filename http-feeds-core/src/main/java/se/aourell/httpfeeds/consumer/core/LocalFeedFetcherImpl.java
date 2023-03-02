package se.aourell.httpfeeds.consumer.core;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumer;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.producer.core.CloudEventMapper;
import se.aourell.httpfeeds.producer.core.FeedItem;
import se.aourell.httpfeeds.producer.spi.EventFeedService;
import se.aourell.httpfeeds.producer.spi.EventFeedsRegistry;
import se.aourell.httpfeeds.util.Result;

import java.util.List;

public class LocalFeedFetcherImpl implements LocalFeedFetcher {

  private final EventFeedsRegistry eventFeedsRegistry;
  private final CloudEventMapper cloudEventMapper;

  public LocalFeedFetcherImpl(EventFeedsRegistry eventFeedsRegistry, CloudEventMapper cloudEventMapper) {
    this.eventFeedsRegistry = eventFeedsRegistry;
    this.cloudEventMapper = cloudEventMapper;
  }

  @Override
  public Result<List<CloudEvent>> fetchLocalEvents(FeedConsumer feedConsumer) {
    try {
      final EventFeedService eventFeedService = eventFeedsRegistry.getLocalFeedByName(feedConsumer.getFeedName())
        .orElseThrow(() -> new RuntimeException("Unable to find defined feed item service by name: " + feedConsumer.getFeedName()));

      final List<FeedItem> feedItems = feedConsumer.getLastProcessedId()
        .map(lastProcessedId -> eventFeedService.fetch(lastProcessedId, null))
        .orElseGet(() -> eventFeedService.fetch(null, null));

      final List<CloudEvent> result = feedItems.stream()
        .map(cloudEventMapper::mapFeedItem)
        .toList();

      return Result.success(result);
    } catch (Throwable e) {
      return Result.failure(e);
    }
  }
}
