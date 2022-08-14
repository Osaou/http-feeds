package se.aourell.httpfeeds.client.spi;

import se.aourell.httpfeeds.core.CloudEvent;

import java.util.List;

public interface HttpFeedsClient {

  List<CloudEvent> pollCloudEvents(String url, String lastProcessedId);

  default List<CloudEvent> pollCloudEvents(String url) {
    return pollCloudEvents(url, null);
  }
}
