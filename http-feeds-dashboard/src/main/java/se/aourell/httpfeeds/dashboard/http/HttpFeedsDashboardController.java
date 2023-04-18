package se.aourell.httpfeeds.dashboard.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.aourell.httpfeeds.dashboard.jte.JteRenderer;
import se.aourell.httpfeeds.tracing.core.ShelvedTrace;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.util.PagedList;

import java.util.Map;
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
}
