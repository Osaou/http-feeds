package se.aourell.httpfeeds.spi;

import se.aourell.httpfeeds.core.FeedItem;

import java.util.List;
import java.util.Optional;

public interface FeedItemService {

  List<FeedItem> fetch(Optional<String> lastEventId);

  List<FeedItem> fetchWithTimeout(Optional<String> lastEventId, Long timeoutMillis);
}
