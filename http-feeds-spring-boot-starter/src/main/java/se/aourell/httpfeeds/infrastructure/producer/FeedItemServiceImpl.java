package se.aourell.httpfeeds.infrastructure.producer;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.infrastructure.spring.autoconfigure.ProducerProperties;
import se.aourell.httpfeeds.producer.core.FeedItem;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;
import se.aourell.httpfeeds.producer.spi.FeedItemService;

public class FeedItemServiceImpl implements FeedItemService {

  private static final Logger LOG = LoggerFactory.getLogger(FeedItemServiceImpl.class);

  private final FeedItemRepository feedItemRepository;
  private final Duration pollInterval;
  private final int limit;

  public FeedItemServiceImpl(FeedItemRepository feedItemRepository, ProducerProperties producerProperties) {
    this.feedItemRepository = feedItemRepository;
    this.pollInterval = producerProperties.getPollInterval();
    this.limit = producerProperties.getLimit();
  }

  @Override
  public List<FeedItem> fetch(String lastEventId, String subjectId) {
    return Optional.ofNullable(lastEventId)
      .map(lastId -> fetchFromEventId(lastId, subjectId))
      .orElseGet(() -> fetchFromBeginning(subjectId));
  }

  private List<FeedItem> fetchFromEventId(String eventId, String subjectId) {
    return Optional.ofNullable(subjectId)
      .map(subject -> feedItemRepository.findByIdGreaterThanForSubject(eventId, subject, limit))
      .orElseGet(() -> feedItemRepository.findByIdGreaterThan(eventId, limit));
  }

  private List<FeedItem> fetchFromBeginning(String subjectId) {
    return Optional.ofNullable(subjectId)
      .map(subject -> feedItemRepository.findAllForSubject(subject, limit))
      .orElseGet(() -> feedItemRepository.findAll(limit));
  }

  @Override
  public List<FeedItem> fetchWithTimeout(String lastEventId, String subjectId, Long timeoutMillis) {
    final Instant timeoutTimestamp = Instant.now().plus(timeoutMillis, ChronoUnit.MILLIS);

    while (true) {
      final var items = fetch(lastEventId, subjectId);
      if (!items.isEmpty()) {
        return items;
      }

      if (Instant.now().isAfter(timeoutTimestamp)) {
        // polling timed out, return empty response
        return List.of();
      }

      try {
        // no items found, wait {pollInterval} milliseconds and retry
        //noinspection BusyWait
        Thread.sleep(pollInterval.toMillis());
      } catch (InterruptedException e) {
        LOG.trace("Thread was interrupted. Probably a graceful shutdown. Attempting to send empty response.");
        return List.of();
      }
    }
  }
}
