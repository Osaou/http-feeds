package se.aourell.httpfeeds.core;

import java.util.List;

public interface FeedService {

  List<FeedItem> fetchFeedItems(String lastEventId);

  List<FeedItem> fetchFeedItemsWithPolling(String lastEventId, Long timeoutMillis);
}
