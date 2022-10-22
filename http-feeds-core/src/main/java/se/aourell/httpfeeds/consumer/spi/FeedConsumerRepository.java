package se.aourell.httpfeeds.consumer.spi;

import java.util.Optional;

public interface FeedConsumerRepository {

  Optional<String> retrieveLastProcessedId(String feedName);

  void storeLastProcessedId(String feedName, String id);
}
