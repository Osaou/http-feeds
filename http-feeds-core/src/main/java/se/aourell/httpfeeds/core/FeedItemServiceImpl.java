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

  private static final Logger LOG = LoggerFactory.getLogger(FeedItemServiceImpl.class);
  private static final Duration POLL_INTERVAL = Duration.of(1, ChronoUnit.SECONDS);
  private static final long LIMIT_COUNT_PER_REQUEST = 1000;

  private final FeedItemRepository feedItemRepository;

  public FeedItemServiceImpl(FeedItemRepository feedItemRepository) {
    this.feedItemRepository = feedItemRepository;
  }

  @Override
  public List<FeedItem> fetch(Optional<String> lastEventId) {
    LOG.debug("Find items with lastEventId={}", lastEventId);
    return lastEventId
      .map(lastId -> feedItemRepository.findByIdGreaterThan(lastId, LIMIT_COUNT_PER_REQUEST))
      .orElseGet(() -> feedItemRepository.findAll(LIMIT_COUNT_PER_REQUEST));
  }

  @Override
  public List<FeedItem> fetchWithTimeout(Optional<String> lastEventId, Long timeoutMillis) {
    LOG.debug("Long polling for items with lastEventId={} timeoutMillis={}", lastEventId, timeoutMillis);
    final Instant timeoutTimestamp = Instant.now().plus(timeoutMillis, MILLIS);

    while (true) {
      final var items = lastEventId
        .map(lastId -> feedItemRepository.findByIdGreaterThan(lastId, LIMIT_COUNT_PER_REQUEST))
        .orElseGet(() -> feedItemRepository.findAll(LIMIT_COUNT_PER_REQUEST));

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
        LOG.debug("No items found. Wait {} and then retry again.", POLL_INTERVAL);
        //noinspection BusyWait
        Thread.sleep(POLL_INTERVAL.toMillis());
      } catch (InterruptedException e) {
        LOG.debug("Thread was interrupted. Probably a graceful shutdown. Try to send empty response.");
        return List.of();
      }
    }
  }
}
