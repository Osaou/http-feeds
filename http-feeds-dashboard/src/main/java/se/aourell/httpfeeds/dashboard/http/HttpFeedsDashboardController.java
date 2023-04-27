package se.aourell.httpfeeds.dashboard.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.aourell.httpfeeds.dashboard.jte.JteRenderer;
import se.aourell.httpfeeds.producer.core.EventFeedsUtil;
import se.aourell.httpfeeds.tracing.core.ShelvedTrace;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.util.PagedList;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
public class HttpFeedsDashboardController {

  private final DeadLetterQueueRepository deadLetterQueueRepository;
  private final JteRenderer jte;

  public HttpFeedsDashboardController(DeadLetterQueueRepository deadLetterQueueRepository, JteRenderer jte) {
    this.deadLetterQueueRepository = deadLetterQueueRepository;
    this.jte = jte;
  }

  @GetMapping(value = EventFeedsUtil.PATH_PREFIX + "/dashboard", produces = "text/html")
  public String dashboard() {
    return jte.view("pages/dashboard");
  }

  @GetMapping(value = EventFeedsUtil.PATH_PREFIX + "/dashboard/traces", produces = "text/html")
  public String traces(@RequestParam Optional<Integer> page) {
    int p = Math.max(1, page.orElse(1));
    final PagedList<ShelvedTrace> traces = deadLetterQueueRepository.listTracesWithPagination(p);

    return jte.view("components/traces", Map.of("traces", traces));
  }

  @PostMapping(value = EventFeedsUtil.PATH_PREFIX + "/dashboard/traces/{traceId}/redeliver", produces = "text/html")
  public String redeliverTrace(@PathVariable String traceId) {
    String message;
    try {
      Objects.requireNonNull(traceId);
      deadLetterQueueRepository.reintroduceForDelivery(traceId);

      message = "Successfully re-introduced";
    } catch (Exception e) {
      message = "Failure: " + e.getMessage();
    }

    return jte.view("components/message", Map.of("message", message));
  }

  @GetMapping(value = EventFeedsUtil.PATH_PREFIX + "/dashboard/traces/{traceId}/status", produces = "text/html")
  public String checkTraceStatus(@PathVariable String traceId, @RequestParam Optional<Boolean> editing) {
    Objects.requireNonNull(traceId);
    final Optional<ShelvedTrace> trace = deadLetterQueueRepository.checkTraceStatus(traceId);

    return jte.view("components/trace", Map.of(
      "trace", trace.orElseThrow(),
      "editing", editing.orElse(false)));
  }

  @PostMapping(value = EventFeedsUtil.PATH_PREFIX + "/dashboard/traces/{traceId}/update", produces = "text/html")
  public String updateEventData(@PathVariable String traceId) {
    Objects.requireNonNull(traceId);
    return checkTraceStatus(traceId, Optional.of(false));
  }
}
