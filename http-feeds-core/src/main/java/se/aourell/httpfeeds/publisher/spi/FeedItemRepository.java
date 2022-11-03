package se.aourell.httpfeeds.publisher.spi;

import se.aourell.httpfeeds.publisher.core.FeedItem;

import java.util.List;

public interface FeedItemRepository {

  String DEFAULT_TABLE_NAME = "eventfeeds";

  List<FeedItem> findAll(int limit);

  List<FeedItem> findByIdGreaterThan(String lastEventId, int limit);

  List<FeedItem> findAllForSubject(String subject, int limit);

  List<FeedItem> findByIdGreaterThanForSubject(String lastEventId, String subject, int limit);

  void append(FeedItem feedItem);
}
