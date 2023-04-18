package se.aourell.httpfeeds.tracing.core;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.List;

public class DeadLetterQueueService {

  private final DeadLetterQueueRepository deadLetterQueueRepository;

  public DeadLetterQueueService(DeadLetterQueueRepository deadLetterQueueRepository) {
    this.deadLetterQueueRepository = deadLetterQueueRepository;
  }

  public void shelveFromFeed(CloudEvent event, String feedConsumerName, Throwable lastError) {
    final var exceptionStringifier = new StringWriter();
    lastError.printStackTrace(new PrintWriter(exceptionStringifier));

    final String traceId = event.traceId()
      .orElseGet(event::id);

    final var trace = new ShelvedTrace(traceId, feedConsumerName, OffsetDateTime.now(), exceptionStringifier.toString(), List.of(event));
    deadLetterQueueRepository.shelveFromFeed(trace);
  }

  public List<CloudEvent> findReintroduced(String feedConsumerName) {
    final var reintroduced = deadLetterQueueRepository.findReintroduced(feedConsumerName);
    if (reintroduced.isEmpty()) {
      return List.of();
    }

    return reintroduced.stream()
      .flatMap(trace -> trace.events().stream())
      .toList();
  }
}
