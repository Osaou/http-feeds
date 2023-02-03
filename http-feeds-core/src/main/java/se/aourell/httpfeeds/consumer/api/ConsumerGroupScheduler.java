package se.aourell.httpfeeds.consumer.api;

import java.util.function.Consumer;

public interface ConsumerGroupScheduler {

  ConsumerGroupScheduler scheduleGroup(String groupName, Consumer<ConsumerGroupCreator> consumerGroup);
}
