package se.aourell.httpfeeds.spi;

import se.aourell.httpfeeds.core.FeedItem;

import java.time.Instant;
import java.util.List;

public interface FeedItemRepository {

  List<FeedItem> findAll(int limit);

  List<FeedItem> findByIdGreaterThan(String lastEventId, int limit);

  void append(String id, String type, String source, Instant time, String subject, String method, String data);
}
