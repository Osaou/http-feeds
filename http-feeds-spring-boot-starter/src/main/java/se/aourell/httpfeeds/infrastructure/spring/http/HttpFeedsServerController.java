package se.aourell.httpfeeds.infrastructure.spring.http;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.aourell.httpfeeds.producer.core.CloudEventMapper;
import se.aourell.httpfeeds.producer.core.EventFeedsUtil;
import se.aourell.httpfeeds.producer.spi.CloudEventSerializer;
import se.aourell.httpfeeds.producer.spi.EventFeedsRegistry;
import se.aourell.httpfeeds.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
public class HttpFeedsServerController {

  private final EventFeedsRegistry feedRegistry;
  private final CloudEventMapper cloudEventMapper;
  private final CloudEventSerializer cloudEventSerializer;

  public HttpFeedsServerController(EventFeedsRegistry feedRegistry, CloudEventMapper cloudEventMapper, CloudEventSerializer cloudEventSerializer) {
    this.feedRegistry = Assert.notNull(feedRegistry);
    this.cloudEventMapper = Assert.notNull(cloudEventMapper);
    this.cloudEventSerializer = Assert.notNull(cloudEventSerializer);
  }

  @GetMapping(
    value = EventFeedsUtil.PATH_PREFIX + "/**",
    produces = { "application/cloudevents-batch+json", MediaType.APPLICATION_JSON_VALUE })
  public String getFeedItems(@RequestParam(name = "lastEventId", required = false) String lastEventId,
                             @RequestParam(name = "timeout", required = false) Long timeoutMillis,
                             @RequestParam(name = "subject", required = false) String subjectId,
                             HttpServletRequest request) {
    final var path = request.getServletPath();

    final var feedItemService = feedRegistry.getPublishedHttpFeedByPath(path)
      .orElseThrow(() -> new IllegalArgumentException(String.format("No published http-feed defined for feedPath \"%s\"", path)));

    final var feedItems = Optional.ofNullable(timeoutMillis)
      .map(timeout -> feedItemService.fetchWithTimeout(lastEventId, subjectId, timeout))
      .orElseGet(() -> feedItemService.fetch(lastEventId, subjectId));

    final var cloudEvents = feedItems
      .stream()
      .map(cloudEventMapper::mapFeedItem)
      .toList();

    return cloudEventSerializer.toString(cloudEvents);
  }
}
