package se.aourell.httpfeeds.publisher.spi;

import java.util.function.BiFunction;

public interface FeedItemRepositoryFactory extends BiFunction<String, String, FeedItemRepository> { }
