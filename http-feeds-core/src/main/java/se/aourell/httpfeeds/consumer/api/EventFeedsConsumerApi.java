package se.aourell.httpfeeds.consumer.api;

import java.util.function.Consumer;

public interface EventFeedsConsumerApi {

  EventFeedsConsumerApi scheduleConsumerGroup(String groupName, Consumer<ConsumerGroupCreator> consumerGroup);
}
