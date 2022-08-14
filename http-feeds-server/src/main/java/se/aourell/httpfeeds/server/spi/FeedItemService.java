package se.aourell.httpfeeds.server.spi;

import se.aourell.httpfeeds.server.core.FeedItem;

import java.time.Duration;
import java.util.List;

public interface FeedItemService {

  Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(1);
  int DEFAULT_LIMIT_COUNT_PER_REQUEST = 1000;

  List<FeedItem> fetch(String lastEventId);

  List<FeedItem> fetchWithTimeout(String lastEventId, Long timeoutMillis);
}
