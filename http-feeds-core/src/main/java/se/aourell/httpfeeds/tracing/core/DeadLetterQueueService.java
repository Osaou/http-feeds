package se.aourell.httpfeeds.tracing.core;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.util.Assert;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Predicate;

public class DeadLetterQueueService {

  private final DeadLetterQueueRepository deadLetterQueueRepository;

  public DeadLetterQueueService(DeadLetterQueueRepository deadLetterQueueRepository) {
    this.deadLetterQueueRepository = Assert.notNull(deadLetterQueueRepository);
  }

  public void shelveFromFeed(CloudEvent event, String feedConsumerName, Throwable lastError) {
    final var exceptionStringifier = new StringWriter();
    lastError.printStackTrace(new PrintWriter(exceptionStringifier));

    final String traceId = event.traceId()
      .filter(Predicate.not(String::isBlank))
      .orElseGet(event::id);

    final var trace = new ShelvedTrace(traceId, feedConsumerName, OffsetDateTime.now(), exceptionStringifier.toString(), false, List.of(event));
    deadLetterQueueRepository.shelveFromFeed(trace);
  }

  public void reShelveAndUpdateCause(String traceId, Throwable updatedError) {
    final var exceptionStringifier = new StringWriter();
    updatedError.printStackTrace(new PrintWriter(exceptionStringifier));

    deadLetterQueueRepository.reShelveAndUpdateCause(traceId, exceptionStringifier.toString());
  }
}
