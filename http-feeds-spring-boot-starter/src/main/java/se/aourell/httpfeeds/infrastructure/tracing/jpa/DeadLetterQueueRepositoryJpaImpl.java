package se.aourell.httpfeeds.infrastructure.tracing.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.spi.CloudEventDeserializer;
import se.aourell.httpfeeds.producer.spi.CloudEventSerializer;
import se.aourell.httpfeeds.tracing.core.ShelvedTrace;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.util.PagedList;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DeadLetterQueueRepositoryJpaImpl implements DeadLetterQueueRepository {

  private final CloudEventSerializer cloudEventSerializer;
  private final CloudEventDeserializer cloudEventDeserializer;
  private final DeadLetterQueueSpringRepository deadLetterQueueSpringRepository;

  public DeadLetterQueueRepositoryJpaImpl(CloudEventSerializer cloudEventSerializer, CloudEventDeserializer cloudEventDeserializer, DeadLetterQueueSpringRepository deadLetterQueueSpringRepository) {
    this.cloudEventSerializer = cloudEventSerializer;
    this.cloudEventDeserializer = cloudEventDeserializer;
    this.deadLetterQueueSpringRepository = deadLetterQueueSpringRepository;
  }

  @Override
  public PagedList<ShelvedTrace> listTracesWithPagination(int page) {
    final Page<DeadLetterQueueEntity> pagedTraces = deadLetterQueueSpringRepository.findAllBy(PageRequest.of(page - 1, 3));
    final List<ShelvedTrace> traces = pagedTraces
      .map(this::mapFromEntityToShelvedTrace)
      .toList();

    return new PagedList<>(traces, page, pagedTraces.getTotalPages(), pagedTraces.getTotalElements());
  }

  @Override
  public List<ShelvedTrace> findReintroduced(String feedConsumerName) {
    return deadLetterQueueSpringRepository.findTop10ByFeedConsumerNameAndAttemptReprocessingIsTrue(feedConsumerName)
      .stream()
      .map(this::mapFromEntityToShelvedTrace)
      .toList();
  }

  @Override
  public boolean isTraceShelved(String traceId) {
    return deadLetterQueueSpringRepository.existsById(traceId);
  }

  @Override
  public void shelveFromFeed(ShelvedTrace trace) {
    final var dlqTrace = new DeadLetterQueueEntity();
    final var events = trace.events()
      .stream()
      .map(event -> {
        final String serializedEvent = cloudEventSerializer.toString(event);

        final var dlqEvent = new DeadLetterQueueEventEntity();
        dlqEvent.setEventId(event.id());
        dlqEvent.setTrace(dlqTrace);
        dlqEvent.setData(serializedEvent);

        return dlqEvent;
      })
      .collect(Collectors.toSet());

    dlqTrace.setTraceId(trace.traceId());
    dlqTrace.setFeedConsumerName(trace.feedConsumerName());
    dlqTrace.setShelvedTime(trace.shelvedTime());
    dlqTrace.setLastKnownError(trace.lastKnownError());
    dlqTrace.setAttemptReprocessing(false);
    dlqTrace.setEvents(events);

    deadLetterQueueSpringRepository.save(dlqTrace);
  }

  @Override
  public void addEventToShelvedTrace(String traceId, CloudEvent event) {
    deadLetterQueueSpringRepository.findById(traceId)
      .ifPresent(dlqTrace -> {
        final String serializedEvent = cloudEventSerializer.toString(event);

        final var dlqEvent = new DeadLetterQueueEventEntity();
        dlqEvent.setEventId(event.id());
        dlqEvent.setTrace(dlqTrace);
        dlqEvent.setData(serializedEvent);

        dlqTrace.getEvents().add(dlqEvent);
        deadLetterQueueSpringRepository.save(dlqTrace);
      });
  }

  @Override
  public void reintroduceForDelivery(String traceId) {
    deadLetterQueueSpringRepository.findById(traceId)
      .ifPresent(dlqTrace -> {
        dlqTrace.setAttemptReprocessing(true);
        deadLetterQueueSpringRepository.save(dlqTrace);
      });
  }

  @Override
  public void keepShelved(String traceId) {
    deadLetterQueueSpringRepository.findById(traceId)
      .ifPresent(dlqTrace -> {
        dlqTrace.setAttemptReprocessing(false);
        deadLetterQueueSpringRepository.save(dlqTrace);
      });
  }

  @Override
  public void markDelivered(String traceId, String eventId) {
    deadLetterQueueSpringRepository.findById(traceId)
      .ifPresent(dlqTrace -> {
        dlqTrace.getEvents()
          .removeIf(dlqEvent -> dlqEvent.getEventId().equals(eventId));

        if (dlqTrace.getEvents().isEmpty()) {
          deadLetterQueueSpringRepository.deleteById(traceId);
        } else {
          deadLetterQueueSpringRepository.save(dlqTrace);
        }
      });
  }

  private ShelvedTrace mapFromEntityToShelvedTrace(DeadLetterQueueEntity entity) {
    return new ShelvedTrace(entity.getTraceId(), entity.getFeedConsumerName(), entity.getShelvedTime(), entity.getLastKnownError(), entity.getEvents()
      .stream()
      .map(this::mapFromEntityToCloudEvent)
      .sorted(Comparator.comparing(CloudEvent::id))
      .toList());
  }

  private CloudEvent mapFromEntityToCloudEvent(DeadLetterQueueEventEntity entity) {
    return cloudEventDeserializer.toCloudEvent(entity.getData());
  }
}
