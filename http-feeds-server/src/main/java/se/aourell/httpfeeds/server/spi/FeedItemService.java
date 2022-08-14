package se.aourell.httpfeeds.server.spi;

import se.aourell.httpfeeds.server.core.FeedItem;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface FeedItemService {

  Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(1);
  int DEFAULT_LIMIT_COUNT_PER_REQUEST = 1000;

  List<FeedItem> fetch(Optional<String> lastEventId);

  List<FeedItem> fetchWithTimeout(Optional<String> lastEventId, Long timeoutMillis);
}
