package se.aourell.httpfeeds.consumer.api;

import java.util.function.Consumer;

public interface ConsumerGroupCreator {

  ConsumerGroupCreator defineLocalConsumer(String feedName, Consumer<ConsumerCreator> consumer);
  ConsumerGroupCreator defineLocalConsumer(String feedName, String feedConsumerName, Consumer<ConsumerCreator> consumer);

  ConsumerGroupCreator defineRemoteConsumer(String feedName, String baseUri, Consumer<ConsumerCreator> consumer);
  ConsumerGroupCreator defineRemoteConsumer(String feedName, String feedConsumerName, String completeFeedUrl, Consumer<ConsumerCreator> consumer);
}
