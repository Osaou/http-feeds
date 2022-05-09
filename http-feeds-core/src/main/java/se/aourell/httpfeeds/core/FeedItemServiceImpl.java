package se.aourell.httpfeeds.core;

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

  private static final Logger LOG = LoggerFactory.getLogger(FeedItemServiceImpl.class);

  private final FeedItemRepository feedItemRepository;
  private final Duration pollInterval;
  private final int limit;

  public FeedItemServiceImpl(FeedItemRepository feedItemRepository, Duration pollInterval, int limit) {
    this.feedItemRepository = feedItemRepository;
    this.pollInterval = pollInterval;
    this.limit = limit;
  }

  @Override
  public List<FeedItem> fetch(Optional<String> lastEventId) {
    LOG.debug("Find items with lastEventId={}", lastEventId);
    return lastEventId
      .map(lastId -> feedItemRepository.findByIdGreaterThan(lastId, limit))
      .orElseGet(() -> feedItemRepository.findAll(limit));
  }

  @Override
  public List<FeedItem> fetchWithTimeout(Optional<String> lastEventId, Long timeoutMillis) {
    LOG.debug("Long polling for items with lastEventId={} timeoutMillis={}", lastEventId, timeoutMillis);
    final Instant timeoutTimestamp = Instant.now().plus(timeoutMillis, ChronoUnit.MILLIS);

    while (true) {
      final var items = lastEventId
        .map(lastId -> feedItemRepository.findByIdGreaterThan(lastId, limit))
        .orElseGet(() -> feedItemRepository.findAll(limit));

      int numberOfItems = items.size();
      if (numberOfItems > 0) {
        LOG.debug("Returning {} items.", numberOfItems);
        return items;
      }

      if (Instant.now().isAfter(timeoutTimestamp)) {
        LOG.debug("Polling timed out. Returning empty response.");
        return List.of();
      }

      try {
        LOG.debug("No items found. Wait {} and then retry again.", pollInterval);
        //noinspection BusyWait
        Thread.sleep(DEFAULT_POLL_INTERVAL.toMillis());
      } catch (InterruptedException e) {
        LOG.debug("Thread was interrupted. Probably a graceful shutdown. Try to send empty response.");
        return List.of();
      }
    }
  }
}
