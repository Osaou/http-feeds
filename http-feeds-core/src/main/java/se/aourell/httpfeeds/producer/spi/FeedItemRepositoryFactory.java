package se.aourell.httpfeeds.producer.spi;

import java.util.function.BiFunction;

public interface FeedItemRepositoryFactory extends BiFunction<String, String, FeedItemRepository> { }
