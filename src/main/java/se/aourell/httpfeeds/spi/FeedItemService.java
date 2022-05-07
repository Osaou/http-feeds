package se.aourell.httpfeeds.spi;

import se.aourell.httpfeeds.core.FeedItem;

import java.util.List;

public interface FeedItemService {

  List<FeedItem> fetch(String lastEventId);

  List<FeedItem> fetchWithPolling(String lastEventId, Long timeoutMillis);
}
