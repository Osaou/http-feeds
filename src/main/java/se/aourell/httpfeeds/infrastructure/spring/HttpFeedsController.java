package se.aourell.httpfeeds.infrastructure.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.aourell.httpfeeds.core.CloudEvent;
import se.aourell.httpfeeds.core.HttpFeedDefinition;
import se.aourell.httpfeeds.spi.HttpFeedRegistry;

import java.util.List;

@RestController
public class HttpFeedsController {

  private static final Logger log = LoggerFactory.getLogger(HttpFeedsController.class);
  private final HttpFeedRegistry feedRegistry;
  private final CloudEvent.Mapper cloudEventMapper;

  public HttpFeedsController(HttpFeedRegistry feedRegistry, CloudEvent.Mapper cloudEventMapper) {
    this.feedRegistry = feedRegistry;
    this.cloudEventMapper = cloudEventMapper;
  }

  @GetMapping(value = "/feed/{feedName}", produces = {"application/cloudevents-batch+json", "application/json"})
  public List<CloudEvent> getFeedItems(
    @PathVariable String feedName,
    @RequestParam(name = "lastEventId", required = false) String lastEventId,
    @RequestParam(name = "timeout", required = false) Long timeoutMillis
  ) {
    log.debug("GET feed {} with lastEventId {}", feedName, lastEventId);

    final var feedItemService = feedRegistry.getDefinedFeed(feedName)
      .map(HttpFeedDefinition::feedItemService)
      .orElseThrow(IllegalArgumentException::new);

    final var feedItems = switch (timeoutMillis) {
      case null -> feedItemService.fetch(lastEventId);
      default -> feedItemService.fetchWithPolling(lastEventId, timeoutMillis);
    };

    final var cloudEvents = feedItems.stream()
      .map(cloudEventMapper::mapFeedItem)
      .toList();

    log.debug("GET feed with lastEventId {} returned {} events", lastEventId, cloudEvents.size());
    return cloudEvents;
  }
}
