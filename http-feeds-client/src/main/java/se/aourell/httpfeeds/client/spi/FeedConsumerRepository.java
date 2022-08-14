package se.aourell.httpfeeds.client.spi;

import java.util.Optional;

public interface FeedConsumerRepository {

  Optional<String> retrieveLastProcessedId(String feedName);

  void storeLastProcessedId(String feedName, String id);
}
