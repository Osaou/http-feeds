package se.aourell.httpfeeds.producer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.producer.spi.EventFeedService;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;
import se.aourell.httpfeeds.tracing.spi.ApplicationShutdownDetector;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EventFeedServiceImpl implements EventFeedService {

  private static final Logger LOG = LoggerFactory.getLogger(EventFeedServiceImpl.class);

  private final ApplicationShutdownDetector applicationShutdownDetector;
  private final FeedItemRepository feedItemRepository;
  private final String name;
  private final String feedPath;
  private final boolean isPublishedOverHttp;
  private final Duration pollInterval;
  private final int limit;

  public EventFeedServiceImpl(ApplicationShutdownDetector applicationShutdownDetector,
                              FeedItemRepository feedItemRepository,
                              String name,
                              boolean isPublishedOverHttp,
                              Duration pollInterval,
                              int limit) {
    this.applicationShutdownDetector = applicationShutdownDetector;
    this.feedItemRepository = feedItemRepository;
    this.name = name;
    this.feedPath = EventFeedsUtil.urlPathFromFeedName(name);
    this.isPublishedOverHttp = isPublishedOverHttp;
    this.pollInterval = pollInterval;
    this.limit = limit;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getFeedPath() {
    return feedPath;
  }

  @Override
  public boolean isPublishedOverHttp() {
    return isPublishedOverHttp;
  }

  @Override
  public List<FeedItem> fetch(String lastEventId, String subjectId) {
    return lastEventId != null
      ? fetchFromEventId(lastEventId, subjectId)
      : fetchFromBeginning(subjectId);
  }

  private List<FeedItem> fetchFromEventId(String eventId, String subjectId) {
    return subjectId != null
      ? feedItemRepository.findByIdGreaterThanForSubject(eventId, subjectId, limit)
      : feedItemRepository.findByIdGreaterThan(eventId, limit);
  }

  private List<FeedItem> fetchFromBeginning(String subjectId) {
    return subjectId != null
      ? feedItemRepository.findAllForSubject(subjectId, limit)
      : feedItemRepository.findAll(limit);
  }

  @Override
  public List<FeedItem> fetchWithTimeout(String lastEventId, String subjectId, Long timeoutMillis) {
    final Instant timeoutTimestamp = Instant.now()
      .plus(timeoutMillis, ChronoUnit.MILLIS);

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
        if (applicationShutdownDetector.isGracefulShutdown()) {
          return List.of();
        }

        LOG.warn("Unexpectedly interrupted while sleeping/polling. Sending empty response since unable to recover.");
        return List.of();
      }
    }
  }
}
