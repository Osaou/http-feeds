package se.aourell.httpfeeds.consumer.spi;

import java.util.function.Function;

public interface FeedConsumerRepositoryFactory extends Function<String, FeedConsumerRepository> { }
