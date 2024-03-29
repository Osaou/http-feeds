package se.aourell.httpfeeds.consumer.spi;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.util.Result;

import java.util.List;

public interface HttpFeedsClient {

  default
  Result<List<CloudEvent>> pollCloudEvents(String httpFeedUrl) {
    return pollCloudEvents(httpFeedUrl, null);
  }

  Result<List<CloudEvent>> pollCloudEvents(String httpFeedUrl, String lastProcessedId);
}
