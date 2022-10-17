package se.aourell.httpfeeds.server.spi;

import java.util.function.BiFunction;

public interface FeedItemRepositoryFactory extends BiFunction<String, String, FeedItemRepository> { }
