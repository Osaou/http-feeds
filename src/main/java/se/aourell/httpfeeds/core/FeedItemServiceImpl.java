package se.aourell.httpfeeds.core;

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
  public List<FeedItem> fetch(String lastEventId) {
    log.debug("Find items with lastEventId={}", lastEventId);
    return feedItemRepository.findByIdGreaterThan(lastEventId, limit);
  }

  @Override
  public List<FeedItem> fetchWithPolling(String lastEventId, Long timeoutMillis) {
    log.debug("Long polling for items with lastEventId={} timeoutMillis={}", lastEventId, timeoutMillis);
    Instant timeoutTimestamp = Instant.now().plus(timeoutMillis, MILLIS);
    List<FeedItem> items;
    while (true) {
      items = feedItemRepository.findByIdGreaterThan(lastEventId, limit);

      int numberOfItems = items.size();
      if (numberOfItems > 0) {
        log.debug("Returning {} items.", numberOfItems);
        return items;
      }

      if (Instant.now().isAfter(timeoutTimestamp)) {
        log.debug("Polling timed out. Returning the empty response.");
        return items;
      }

      try {
        log.debug("No items found. Wait {} and then retry again.", pollInterval);
        //noinspection BusyWait
        Thread.sleep(pollInterval.toMillis());
      } catch (InterruptedException e) {
        log.debug("Thread was interrupted. Probably a graceful shutdown. Try to send response.");
        return items;
      }
    }
  }
}
