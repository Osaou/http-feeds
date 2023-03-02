package se.aourell.httpfeeds.consumer.spi;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumer;
import se.aourell.httpfeeds.util.Result;

import java.util.List;

public interface LocalFeedFetcher {

  Result<List<CloudEvent>> fetchLocalEvents(FeedConsumer feedConsumer);
}
