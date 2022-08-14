package se.aourell.httpfeeds.client.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.client.spi.EventDeserializer;
import se.aourell.httpfeeds.client.spi.FeedConsumerProcessor;
import se.aourell.httpfeeds.client.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.client.spi.HttpFeedsClient;
import se.aourell.httpfeeds.core.CloudEvent;

import java.util.ArrayList;
import java.util.List;

public class FeedConsumerProcessorImpl implements FeedConsumerProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(FeedConsumerProcessorImpl.class);

  private final HttpFeedsClient httpFeedsClient;
  private final EventDeserializer eventDeserializer;
  private final FeedConsumerRepository feedConsumerRepository;
  private final List<FeedConsumerDefinition> consumerDefinitions = new ArrayList<>();

  public FeedConsumerProcessorImpl(HttpFeedsClient httpFeedsClient, EventDeserializer eventDeserializer, FeedConsumerRepository feedConsumerRepository) {
    this.httpFeedsClient = httpFeedsClient;
    this.eventDeserializer = eventDeserializer;
    this.feedConsumerRepository = feedConsumerRepository;
  }

  @Override
  public FeedConsumerDefinition defineConsumer(String feedName, String url, Object bean) {
    final var lastProcessedId = feedConsumerRepository.retrieveLastProcessedId(feedName);
    final var consumerDefinition = new FeedConsumerDefinition(feedName, url, bean, eventDeserializer, lastProcessedId.orElse(null));
    consumerDefinitions.add(consumerDefinition);

    return consumerDefinition;
  }

  @Override
  public void batchPollAndProcessEvents() {
    for (final var consumerDefinition : consumerDefinitions) {
      final var url = consumerDefinition.getUrl();
      final var events = consumerDefinition.getLastProcessedId()
        .map(lastProcessedId -> httpFeedsClient.pollCloudEvents(url, lastProcessedId))
        .orElseGet(() -> httpFeedsClient.pollCloudEvents(url));

      try {
        for (CloudEvent event : events) {
          consumerDefinition.processEvent(event);
        }
      } catch (Throwable e) {
        LOG.error("Exception when processing event handler", e);
      }

      consumerDefinition.getLastProcessedId()
        .ifPresent(updatedLastProcessedId -> {
          final var feedName = consumerDefinition.getFeedName();
          feedConsumerRepository.storeLastProcessedId(feedName, updatedLastProcessedId);
        });
    }
  }
}
