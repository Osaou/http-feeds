package se.aourell.httpfeeds.server.spi;

import se.aourell.httpfeeds.server.core.FeedItem;

import java.time.Instant;
import java.util.List;

public interface FeedItemRepository {

  List<FeedItem> findAll(int limit);

  List<FeedItem> findByIdGreaterThan(String lastEventId, int limit);

  List<FeedItem> findAllForSubject(String subject, int limit);

  List<FeedItem> findByIdGreaterThanForSubject(String lastEventId, String subject, int limit);

  void append(String id, String type, Instant time, String subject, String method, String data);
}
