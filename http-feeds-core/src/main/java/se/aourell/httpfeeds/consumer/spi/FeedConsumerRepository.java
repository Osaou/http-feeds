package se.aourell.httpfeeds.consumer.spi;

import java.util.Optional;

public interface FeedConsumerRepository {

  Optional<String> retrieveLastProcessedId(String feedConsumerName);

  void storeLastProcessedId(String feedConsumerName, String id);
}
