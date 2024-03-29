package se.aourell.httpfeeds.consumer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumer;
import se.aourell.httpfeeds.consumer.spi.HttpFeedsClient;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;
import se.aourell.httpfeeds.util.Assert;
import se.aourell.httpfeeds.util.Result;

import java.util.List;

public class RemoteFeedFetcherImpl implements RemoteFeedFetcher {

  private static final Logger LOG = LoggerFactory.getLogger(RemoteFeedFetcherImpl.class);

  private final HttpFeedsClient httpFeedsClient;

  public RemoteFeedFetcherImpl(HttpFeedsClient httpFeedsClient) {
    this.httpFeedsClient = Assert.notNull(httpFeedsClient);
  }

  @Override
  public Result<List<CloudEvent>> fetchRemoteEvents(FeedConsumer feedConsumer) {
    final String url = feedConsumer.getUrl();
    return feedConsumer.getLastProcessedId()
      .map(lastProcessedId -> httpFeedsClient.pollCloudEvents(url, lastProcessedId))
      .orElseGet(() -> httpFeedsClient.pollCloudEvents(url))
      .peekFailure(e -> LOG.debug("Exception when fetching events", e));
  }
}
