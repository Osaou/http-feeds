package se.aourell.httpfeeds.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.aourell.httpfeeds.core.CloudEvent;
import se.aourell.httpfeeds.core.CloudEventMapper;
import se.aourell.httpfeeds.core.FeedItem;
import se.aourell.httpfeeds.core.HttpFeedDefinition;
import se.aourell.httpfeeds.core.HttpFeedRegistry;

import java.util.List;

@RestController
public class HttpFeedsController {

  private static final Logger log = LoggerFactory.getLogger(HttpFeedsController.class);
  private final HttpFeedRegistry feedRegistry;

  public HttpFeedsController(HttpFeedRegistry feedRegistry) {
    this.feedRegistry = feedRegistry;
  }

  @GetMapping(value = "/feed/{feedName}", produces = {"application/cloudevents-batch+json", "application/json"})
  public List<CloudEvent> getFeedItems(
    @PathVariable String feedName,
    @RequestParam(name = "lastEventId", required = false) String lastEventId,
    @RequestParam(name = "timeout", required = false) Long timeoutMillis
  ) {
    log.debug("GET feed {} with lastEventId {}", feedName, lastEventId);

    final var feedService = feedRegistry.getDefinedFeed(feedName)
      .map(HttpFeedDefinition::feedService)
      .orElseThrow(IllegalArgumentException::new);

    List<FeedItem> items;
    if (timeoutMillis == null) {
      items = feedService.fetchFeedItems(lastEventId);
    } else {
      items = feedService.fetchFeedItemsWithPolling(lastEventId, timeoutMillis);
    }

    final List<CloudEvent> cloudEvents = items.stream()
      .map(CloudEventMapper::toCloudEvent)
      .toList();

    log.debug("GET feed with lastEventId {} returned {} events", lastEventId, cloudEvents.size());
    return cloudEvents;
  }
}
