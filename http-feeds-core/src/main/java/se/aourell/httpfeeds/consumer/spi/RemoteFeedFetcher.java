package se.aourell.httpfeeds.consumer.spi;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;
import se.aourell.httpfeeds.util.Result;

import java.util.List;

public interface RemoteFeedFetcher {

  Result<List<CloudEvent>> fetchRemoteEvents(FeedConsumerProcessor feedConsumerProcessor);
}
