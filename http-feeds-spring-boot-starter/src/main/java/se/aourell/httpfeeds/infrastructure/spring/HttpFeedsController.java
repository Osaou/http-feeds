package se.aourell.httpfeeds.infrastructure.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.aourell.httpfeeds.core.CloudEvent;
import se.aourell.httpfeeds.core.HttpFeedDefinition;
import se.aourell.httpfeeds.spi.HttpFeedRegistry;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
public class HttpFeedsController {

  private static final Logger LOG = LoggerFactory.getLogger(HttpFeedsController.class);
  private final HttpFeedRegistry feedRegistry;
  private final CloudEvent.Mapper cloudEventMapper;
  private final ObjectMapper cloudEventJsonMapper;

  public HttpFeedsController(HttpFeedRegistry feedRegistry, CloudEvent.Mapper cloudEventMapper, ObjectMapper cloudEventJsonMapper) {
    this.feedRegistry = feedRegistry;
    this.cloudEventMapper = cloudEventMapper;
    this.cloudEventJsonMapper = cloudEventJsonMapper;
  }

  @GetMapping(value = HttpFeedDefinition.PATH_PREFIX + "**", produces = {"application/cloudevents-batch+json", "application/json"})
  public String getFeedItems(
    @RequestParam(name = "lastEventId", required = false) Optional<String> lastEventId,
    @RequestParam(name = "timeout", required = false) Optional<Long> timeoutMillis,
    HttpServletRequest request
  ) throws JsonProcessingException {
    final var path = request.getServletPath();
    LOG.debug("GET feed {} with lastEventId {}", path, lastEventId);

    final var feedItemService = feedRegistry.getDefinedFeed(path)
      .map(HttpFeedDefinition::feedItemService)
      .orElseThrow(() -> new IllegalArgumentException(String.format("No feed defined for path \"%s\"", path)));

    final var cloudEvents = timeoutMillis
      .map(timeout -> feedItemService.fetchWithTimeout(lastEventId, timeout))
      .orElseGet(() -> feedItemService.fetch(lastEventId))
      .stream()
      .map(cloudEventMapper::mapFeedItem)
      .toList();

    LOG.debug("GET feed with lastEventId {} returned {} events", lastEventId, cloudEvents.size());
    return cloudEventJsonMapper.writeValueAsString(cloudEvents);
  }
}
