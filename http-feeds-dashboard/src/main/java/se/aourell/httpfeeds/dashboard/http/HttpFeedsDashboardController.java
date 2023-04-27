package se.aourell.httpfeeds.dashboard.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.aourell.httpfeeds.dashboard.jte.JteRenderer;
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

  @GetMapping(value = "/httpfeeds/dashboard", produces = "text/html")
  public String dashboard() {
    return jte.view("pages/dashboard");
  }

  @GetMapping(value = "/httpfeeds/dashboard/traces", produces = "text/html")
  public String traces(@RequestParam Optional<Integer> page) {
    int p = Math.max(1, page.orElse(1));
    final PagedList<ShelvedTrace> traces = deadLetterQueueRepository.listTracesWithPagination(p);

    return jte.view("components/traces", Map.of("traces", traces));
  }

  @PostMapping(value = "/httpfeeds/dashboard/traces/{traceId}/redeliver", produces = "text/html")
  public String redeliverTrace(@PathVariable String traceId) {
    try {
      Objects.requireNonNull(traceId);
      deadLetterQueueRepository.reintroduceForDelivery(traceId);

      return jte.view("components/message", Map.of("message", "Successfully re-introduced"));
    } catch (Exception e) {
      return jte.view("components/message", Map.of("message", "Failure: " + e.getMessage()));
    }
  }

  @GetMapping(value = "/httpfeeds/dashboard/traces/{traceId}/status", produces = "text/html")
  public String checkTraceStatus(@PathVariable String traceId, @RequestParam Optional<Boolean> editing) {
    Objects.requireNonNull(traceId);
    final Optional<ShelvedTrace> trace = deadLetterQueueRepository.checkTraceStatus(traceId);

    return jte.view("components/trace", Map.of(
      "trace", trace.orElseThrow(),
      "editing", editing.orElse(false)));
  }

  @PostMapping(value = "/httpfeeds/dashboard/traces/{traceId}/update", produces = "text/html")
  public String updateEventData(@PathVariable String traceId) {
    Objects.requireNonNull(traceId);
    final Optional<ShelvedTrace> trace = deadLetterQueueRepository.checkTraceStatus(traceId);

    return checkTraceStatus(traceId, Optional.of(false));
  }
}
