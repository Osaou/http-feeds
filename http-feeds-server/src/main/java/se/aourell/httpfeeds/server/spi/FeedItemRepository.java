package se.aourell.httpfeeds.server.spi;

import se.aourell.httpfeeds.server.core.FeedItem;

import java.time.Instant;
import java.util.List;

public interface FeedItemRepository {

  List<FeedItem> findAll(int limit);

  List<FeedItem> findByIdGreaterThan(String lastEventId, int limit);

  void append(String id, String type, Instant time, String subject, String method, String data);
}
