package se.aourell.httpfeeds.dashboard.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.dashboard.jte.JteRenderer;
import se.aourell.httpfeeds.producer.core.EventFeedsUtil;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.tracing.core.ShelvedTrace;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.util.Assert;
import se.aourell.httpfeeds.util.PagedList;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
public class HttpFeedsDashboardController {

  private static DomainEventSerializer domainEventSerializer;

  private final DeadLetterQueueRepository deadLetterQueueRepository;
  private final JteRenderer jte;
  private final ObjectMapper jsonValidator;

  public HttpFeedsDashboardController(DeadLetterQueueRepository deadLetterQueueRepository, JteRenderer jte, ObjectMapper jsonValidator, DomainEventSerializer domainEventSerializer) {
    this.deadLetterQueueRepository = Assert.notNull(deadLetterQueueRepository);
    this.jte = Assert.notNull(jte);
    this.jsonValidator = Assert.notNull(jsonValidator);

    // yuck, but apparently I prefer this to creating specific viewmodels for now...
    HttpFeedsDashboardController.domainEventSerializer = Assert.notNull(domainEventSerializer);
  }

  @GetMapping(
    value = EventFeedsUtil.PATH_PREFIX + "/dashboard",
    produces = MediaType.TEXT_HTML_VALUE)
  public String dashboard() {
    return jte.view("pages/dashboard");
  }

  @GetMapping(
    value = EventFeedsUtil.PATH_PREFIX + "/dashboard/traces",
    produces = MediaType.TEXT_HTML_VALUE)
  public String traces(@RequestParam Optional<Integer> page) {
    int p = Math.max(1, page.orElse(1));
    final PagedList<ShelvedTrace> traces = deadLetterQueueRepository.listTracesWithPagination(p);

    return jte.view("components/traces", Map.of("traces", traces));
  }

  @PostMapping(
    value = EventFeedsUtil.PATH_PREFIX + "/dashboard/traces/{traceId}/redeliver",
    produces = MediaType.TEXT_HTML_VALUE)
  public String redeliverTrace(@PathVariable String traceId) {
    Assert.notNull(traceId);

    String message;
    try {
      deadLetterQueueRepository.reIntroduceForDelivery(traceId);
      message = "Successfully re-introduced";
    } catch (Exception e) {
      message = "Failure: " + e.getMessage();
    }

    return jte.view("components/message", Map.of("message", message));
  }

  @GetMapping(
    value = EventFeedsUtil.PATH_PREFIX + "/dashboard/traces/{traceId}",
    produces = MediaType.TEXT_HTML_VALUE)
  public String traceData(@PathVariable String traceId) {
    Assert.notNull(traceId);

    final ShelvedTrace trace = deadLetterQueueRepository.checkTraceStatus(traceId)
      .orElseThrow();

    return jte.view("components/trace", Map.of(
      "trace", trace));
  }

  @GetMapping(
    value = EventFeedsUtil.PATH_PREFIX + "/dashboard/traces/{traceId}/events/{eventId}",
    produces = MediaType.TEXT_HTML_VALUE)
  public String eventData(@PathVariable String traceId,
                          @PathVariable String eventId,
                          @RequestParam Optional<Boolean> edit) {
    Assert.notNull(traceId);
    Assert.notNull(traceId);

    final CloudEvent updated = deadLetterQueueRepository.checkTraceStatus(traceId)
      .orElseThrow()
      .events()
      .stream()
      .filter(event -> event.id().equals(eventId))
      .findFirst()
      .orElseThrow();

    return jte.view("components/event", Map.of(
      "traceId", traceId,
      "event", updated,
      "isEditing", edit.orElse(false)));
  }

  @PutMapping(
    value = EventFeedsUtil.PATH_PREFIX + "/dashboard/traces/{traceId}/events/{eventId}",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_HTML_VALUE)
  public String updateEventData(@PathVariable String traceId,
                                @PathVariable String eventId,
                                String data) throws IOException {
    Assert.notNull(traceId);
    Assert.notNull(traceId);
    Assert.notNull(data);

    decodeAndValidateJson(data).ifPresent(json ->
      deadLetterQueueRepository.mendEventData(eventId, json));

    return eventData(traceId, eventId, Optional.of(false));
  }

  private Optional<String> decodeAndValidateJson(String json) throws JsonProcessingException {
    if (json == null) {
      return Optional.empty();
    }

    //final String decoded = UriUtils.decode(json, StandardCharsets.UTF_8);
    jsonValidator.readTree(json);
    return Optional.of(json);
  }

  public static String serializeEventDataForEditing(Object data) {
    try {
      return domainEventSerializer.toString(data);
    } catch (Exception e) {
      return "<Unable to serialize data!>";
    }
  }
}
