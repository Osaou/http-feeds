package se.aourell.httpfeeds.consumer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessorGroup;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.HttpFeedConsumerRegistry;
import se.aourell.httpfeeds.consumer.spi.HttpFeedsClient;

public class HttpFeedConsumerRegistryImpl implements HttpFeedConsumerRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(HttpFeedConsumerRegistryImpl.class);

  private final FeedConsumerProcessorGroup feedConsumerProcessorGroup;
  private final HttpFeedsClient httpFeedsClient;

  public HttpFeedConsumerRegistryImpl(DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository, HttpFeedsClient httpFeedsClient) {
    this.feedConsumerProcessorGroup = new FeedConsumerProcessorGroup(domainEventDeserializer, feedConsumerRepository);
    this.httpFeedsClient = httpFeedsClient;
  }

  @Override
  public FeedConsumerProcessor defineHttpFeedConsumer(String feedConsumerName, Object bean, String feedName, String feedUrl) {
    return feedConsumerProcessorGroup.defineFeedConsumer(feedConsumerName, bean, feedName, feedUrl);
  }

  @Override
  public void batchPollAndProcessHttpFeedEvents() {
    feedConsumerProcessorGroup.batchFetchAndProcessEvents(processor -> {
      final String url = processor.getUrl();
      return processor.getLastProcessedId()
        .map(lastProcessedId -> httpFeedsClient.pollCloudEvents(url, lastProcessedId))
        .orElseGet(() -> httpFeedsClient.pollCloudEvents(url))
        .peekFailure(e -> LOG.debug("Exception when fetching events", e));
    });
  }
}
