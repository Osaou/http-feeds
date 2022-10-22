package se.aourell.httpfeeds.consumer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.consumer.core.processing.EventFeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.core.processing.LocalFeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.spi.LocalFeedConsumerRegistry;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LocalFeedConsumerRegistryImpl implements LocalFeedConsumerRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(HttpFeedConsumerRegistryImpl.class);

  private final List<LocalFeedConsumerProcessor> localFeedConsumerProcessors = new ArrayList<>();

  @Override
  public EventFeedConsumerProcessor defineLocalConsumer(String feedName, Object bean) {
    final var localFeedConsumerDefinition = new LocalFeedConsumerProcessor(feedName, bean);

    localFeedConsumerProcessors.add(localFeedConsumerDefinition);
    return localFeedConsumerDefinition;
  }

  @Override
  public void processLocalEvent(String id, String subject, Object event, Instant time) {
    for (final var localConsumer : localFeedConsumerProcessors) {
      try {
        localConsumer.processEvent(id, subject, event, time);
      } catch (Exception e) {
        LOG.error("Exception when processing local event", e);
      }
    }
  }
}
