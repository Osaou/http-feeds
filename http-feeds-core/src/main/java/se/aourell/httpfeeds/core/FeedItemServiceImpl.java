package se.aourell.httpfeeds.core;

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.spi.FeedItemService;
import se.aourell.httpfeeds.spi.FeedItemRepository;

public class FeedItemServiceImpl implements FeedItemService {

  private static final Logger log = LoggerFactory.getLogger(FeedItemServiceImpl.class);
  private static final Duration pollInterval = Duration.of(1, ChronoUnit.SECONDS);
  private static final long limit = 1000;

  private final FeedItemRepository feedItemRepository;

  public FeedItemServiceImpl(FeedItemRepository feedItemRepository) {
    this.feedItemRepository = feedItemRepository;
  }

  @Override
  public List<FeedItem> fetch(Optional<String> lastEventId) {
    log.debug("Find items with lastEventId={}", lastEventId);
    return lastEventId
      .map(lastId -> feedItemRepository.findByIdGreaterThan(lastId, limit))
      .orElseGet(() -> feedItemRepository.findAll(limit));
  }

  @Override
  public List<FeedItem> fetchWithTimeout(Optional<String> lastEventId, Long timeoutMillis) {
    log.debug("Long polling for items with lastEventId={} timeoutMillis={}", lastEventId, timeoutMillis);
    final Instant timeoutTimestamp = Instant.now().plus(timeoutMillis, MILLIS);

    while (true) {
      final var items = lastEventId
        .map(lastId -> feedItemRepository.findByIdGreaterThan(lastId, limit))
        .orElseGet(() -> feedItemRepository.findAll(limit));

      int numberOfItems = items.size();
      if (numberOfItems > 0) {
        log.debug("Returning {} items.", numberOfItems);
        return items;
      }

      if (Instant.now().isAfter(timeoutTimestamp)) {
        log.debug("Polling timed out. Returning empty response.");
        return List.of();
      }

      try {
        log.debug("No items found. Wait {} and then retry again.", pollInterval);
        //noinspection BusyWait
        Thread.sleep(pollInterval.toMillis());
      } catch (InterruptedException e) {
        log.debug("Thread was interrupted. Probably a graceful shutdown. Try to send empty response.");
        return List.of();
      }
    }
  }
}
