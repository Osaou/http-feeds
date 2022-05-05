package se.aourell.httpfeeds.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HttpFeedController {

  private static final Logger log = LoggerFactory.getLogger(HttpFeedController.class);
  private final HttpFeedRegistry feedRegistry;

  public HttpFeedController(HttpFeedRegistry feedRegistry) {
    this.feedRegistry = feedRegistry;
  }

  @GetMapping(value = "/feed/{feedName}", produces = {"application/cloudevents-batch+json", "application/json"})
  public List<CloudEvent> getFeedItems(
    @PathVariable String feedName,
    @RequestParam(name = "lastEventId", required = false) String lastEventId,
    @RequestParam(name = "timeout", required = false) Long timeoutMillis
  ) {
    log.debug("GET feed {} with lastEventId {}", feedName, lastEventId);

    final var feedFetcher = feedRegistry.getDefinedFeed(feedName)
      .map(HttpFeedDefinition::feedFetcher)
      .orElseThrow(IllegalArgumentException::new);

    List<FeedItem> items;
    if (timeoutMillis == null) {
      items = feedFetcher.fetch(lastEventId);
    } else {
      items = feedFetcher.fetchWithPolling(lastEventId, timeoutMillis);
    }

    final List<CloudEvent> cloudEvents = items.stream()
      .map(CloudEventMapper::toCloudEvent)
      .toList();

    log.debug("GET feed with lastEventId {} returned {} events", lastEventId, cloudEvents.size());
    return cloudEvents;
  }
}
