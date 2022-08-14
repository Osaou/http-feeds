package se.aourell.httpfeeds.client.spi;

import se.aourell.httpfeeds.core.CloudEvent;
import se.aourell.httpfeeds.core.util.Result;

import java.util.List;

public interface HttpFeedsClient {

  Result<List<CloudEvent>> pollCloudEvents(String httpFeedUrl, String lastProcessedId);

  default Result<List<CloudEvent>> pollCloudEvents(String httpFeedUrl) {
    return pollCloudEvents(httpFeedUrl, null);
  }
}
