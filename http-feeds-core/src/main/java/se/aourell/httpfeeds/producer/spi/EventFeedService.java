package se.aourell.httpfeeds.producer.spi;

import se.aourell.httpfeeds.producer.core.FeedItem;

import java.util.List;

public interface EventFeedService {

  String getName();

  String getFeedPath();

  boolean isPublishedOverHttp();

  List<FeedItem> fetch(String lastEventId, String subjectId);

  List<FeedItem> fetchWithTimeout(String lastEventId, String subjectId, Long timeoutMillis);
}
