package se.aourell.httpfeeds.infrastructure.spring.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.aourell.httpfeeds.producer.core.CloudEventMapper;
import se.aourell.httpfeeds.producer.core.EventFeedDefinition;
import se.aourell.httpfeeds.producer.spi.EventFeedRegistry;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
public class HttpFeedsServerController {

  private final EventFeedRegistry feedRegistry;
  private final CloudEventMapper cloudEventMapper;
  private final ObjectMapper cloudEventObjectMapper;

  public HttpFeedsServerController(EventFeedRegistry feedRegistry, CloudEventMapper cloudEventMapper, ObjectMapper cloudEventObjectMapper) {
    this.feedRegistry = feedRegistry;
    this.cloudEventMapper = cloudEventMapper;
    this.cloudEventObjectMapper = cloudEventObjectMapper;
  }

  @GetMapping(value = EventFeedDefinition.PATH_PREFIX + "**", produces = {"application/cloudevents-batch+json", "application/json"})
  public String getFeedItems(
    @RequestParam(name = "lastEventId", required = false) String lastEventId,
    @RequestParam(name = "timeout", required = false) Long timeoutMillis,
    @RequestParam(name = "subject", required = false) String subjectId,
    HttpServletRequest request
  ) throws JsonProcessingException {
    final var path = request.getServletPath();

    final var feedItemService = feedRegistry.getPublishedHttpFeedByPath(path)
      .map(EventFeedDefinition::feedItemService)
      .orElseThrow(() -> new IllegalArgumentException(String.format("No published http-feed defined for feedPath \"%s\"", path)));

    final var feedItems = Optional.ofNullable(timeoutMillis)
      .map(timeout -> feedItemService.fetchWithTimeout(lastEventId, subjectId, timeout))
      .orElseGet(() -> feedItemService.fetch(lastEventId, subjectId));

    final var cloudEvents = feedItems
      .stream()
      .map(cloudEventMapper::mapFeedItem)
      .toList();

    return cloudEventObjectMapper.writeValueAsString(cloudEvents);
  }
}
