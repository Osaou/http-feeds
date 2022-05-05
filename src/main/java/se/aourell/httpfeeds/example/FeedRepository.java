package se.aourell.httpfeeds.example;

import java.time.Instant;
import java.util.List;

public interface FeedRepository {

  List<FeedItem> findByIdGreaterThan(String lastEventId, long limit);

  void append(String id, String type, Instant time, String subject, String method, String data);
}
