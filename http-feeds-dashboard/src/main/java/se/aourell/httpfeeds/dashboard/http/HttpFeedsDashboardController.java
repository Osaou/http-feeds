package se.aourell.httpfeeds.dashboard.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import se.aourell.httpfeeds.dashboard.jte.JteRenderer;
import se.aourell.httpfeeds.producer.core.EventFeedsUtil;
import se.aourell.httpfeeds.tracing.core.ShelvedTrace;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.util.PagedList;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
public class HttpFeedsDashboardController {

  private static ObjectMapper jsonSerializer;

  private final DeadLetterQueueRepository deadLetterQueueRepository;
  private final JteRenderer jte;
  private final ObjectMapper jsonValidator;

  public HttpFeedsDashboardController(DeadLetterQueueRepository deadLetterQueueRepository, JteRenderer jte, ObjectMapper jsonValidator) {
    this.deadLetterQueueRepository = deadLetterQueueRepository;
    this.jte = jte;
    this.jsonValidator = jsonValidator;

    jsonSerializer = jsonValidator;
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
    Objects.requireNonNull(traceId);

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
    value = EventFeedsUtil.PATH_PREFIX + "/dashboard/traces/{traceId}/status",
    produces = MediaType.TEXT_HTML_VALUE)
  public String checkTraceStatus(@PathVariable String traceId,
                                 @RequestParam Optional<Boolean> editing) {
    Objects.requireNonNull(traceId);
    final Optional<ShelvedTrace> trace = deadLetterQueueRepository.checkTraceStatus(traceId);

    return jte.view("components/trace", Map.of(
      "trace", trace.orElseThrow(),
      "editing", editing.orElse(false)));
  }

  @PutMapping(
    value = EventFeedsUtil.PATH_PREFIX + "/dashboard/traces/{traceId}/mend",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.TEXT_HTML_VALUE)
  public String updateEventData(@PathVariable String traceId,
                                @RequestBody UpdatedEvents events
                                /*@RequestParam("id") List<String> eventIds,
                                @RequestParam("data") List<String> eventData*/) throws JsonProcessingException {
    Objects.requireNonNull(traceId);
    Objects.requireNonNull(events);
    Objects.requireNonNull(events.ids);
    Objects.requireNonNull(events.datas);
    assert events.ids.size() > 0;
    assert events.ids.size() == events.datas.size();

    for (int i = 0; i < events.ids.size(); ++i) {
      final String id = events.ids.get(i);
      final String data = decodeAndValidateJson(events.datas.get(i));
      deadLetterQueueRepository.mendEventData(id, data);
    }

    return checkTraceStatus(traceId, Optional.of(false));
  }

  private String decodeAndValidateJson(String json) throws JsonProcessingException {
    final String decoded = UriUtils.decode(json, StandardCharsets.UTF_8);
    jsonValidator.readTree(decoded);

    return decoded;
  }

  public static String serializeEventDataAsJson(Object data) {
    try {
      return jsonSerializer.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      return "<Unable to serialize data!>";
    }
  }

  private record UpdatedEvents(List<String> ids, List<String> datas) { }
}
