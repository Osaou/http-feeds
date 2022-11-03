package se.aourell.httpfeeds.producer.spi;

import se.aourell.httpfeeds.producer.core.FeedItem;

import java.time.Duration;
import java.util.List;

public interface FeedItemService {

  Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(1);
  int DEFAULT_LIMIT_COUNT_PER_REQUEST = 1000;

  List<FeedItem> fetch(String lastEventId, String subjectId);

  List<FeedItem> fetchWithTimeout(String lastEventId, String subjectId, Long timeoutMillis);
}
