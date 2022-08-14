package se.aourell.httpfeeds.infrastructure.spring.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.aourell.httpfeeds.server.core.CloudEventMapper;
import se.aourell.httpfeeds.server.core.HttpFeedDefinition;
import se.aourell.httpfeeds.server.spi.HttpFeedRegistry;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
public class HttpFeedsServerController {

  private static final Logger LOG = LoggerFactory.getLogger(HttpFeedsServerController.class);
  private final HttpFeedRegistry feedRegistry;
  private final CloudEventMapper cloudEventMapper;
  private final ObjectMapper cloudEventObjectMapper;

  public HttpFeedsServerController(HttpFeedRegistry feedRegistry, CloudEventMapper cloudEventMapper, ObjectMapper cloudEventObjectMapper) {
    this.feedRegistry = feedRegistry;
    this.cloudEventMapper = cloudEventMapper;
    this.cloudEventObjectMapper = cloudEventObjectMapper;
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
    return cloudEventObjectMapper.writeValueAsString(cloudEvents);
  }
}
