package se.aourell.httpfeeds.consumer.api;

public interface FeedConsumerRegistry {

  FeedConsumer registerLocalConsumer(String feedName);

  FeedConsumer registerRemoteConsumer(String feedName);
  FeedConsumer registerRemoteConsumer(String feedName, String baseUri);
}
