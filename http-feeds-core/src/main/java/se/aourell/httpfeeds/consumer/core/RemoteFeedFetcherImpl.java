package se.aourell.httpfeeds.consumer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.spi.HttpFeedsClient;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;
import se.aourell.httpfeeds.util.Result;

import java.util.List;

public class RemoteFeedFetcherImpl implements RemoteFeedFetcher {

  private static final Logger LOG = LoggerFactory.getLogger(RemoteFeedFetcherImpl.class);

  private final HttpFeedsClient httpFeedsClient;

  public RemoteFeedFetcherImpl(HttpFeedsClient httpFeedsClient) {
    this.httpFeedsClient = httpFeedsClient;
  }

  @Override
  public Result<List<CloudEvent>> fetchRemoteEvents(FeedConsumerProcessor feedConsumerProcessor) {
    final String url = feedConsumerProcessor.getUrl();
    return feedConsumerProcessor.getLastProcessedId()
      .map(lastProcessedId -> httpFeedsClient.pollCloudEvents(url, lastProcessedId))
      .orElseGet(() -> httpFeedsClient.pollCloudEvents(url))
      .peekFailure(e -> LOG.debug("Exception when fetching events", e));
  }
}
