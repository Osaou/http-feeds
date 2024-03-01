package se.aourell.httpfeeds.infrastructure.tracing.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.spi.CloudEventDeserializer;
import se.aourell.httpfeeds.producer.spi.CloudEventSerializer;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.tracing.core.ShelvedTrace;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.util.Assert;
import se.aourell.httpfeeds.util.PagedList;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DeadLetterQueueRepositoryJpaImpl implements DeadLetterQueueRepository {

  public static final int TRACES_LISTED_PER_PAGE = 1;

  private final CloudEventSerializer cloudEventSerializer;
  private final CloudEventDeserializer cloudEventDeserializer;
  private final DomainEventSerializer domainEventSerializer;
  private final DeadLetterQueueSpringRepository deadLetterQueueSpringRepository;
  private final DeadLetterQueueEventSpringRepository deadLetterQueueEventSpringRepository;

  public DeadLetterQueueRepositoryJpaImpl(CloudEventSerializer cloudEventSerializer,
                                          CloudEventDeserializer cloudEventDeserializer,
                                          DomainEventSerializer domainEventSerializer,
                                          DeadLetterQueueSpringRepository deadLetterQueueSpringRepository,
                                          DeadLetterQueueEventSpringRepository deadLetterQueueEventSpringRepository) {
    this.cloudEventSerializer = Assert.notNull(cloudEventSerializer);
    this.cloudEventDeserializer = Assert.notNull(cloudEventDeserializer);
    this.domainEventSerializer = Assert.notNull(domainEventSerializer);
    this.deadLetterQueueSpringRepository = Assert.notNull(deadLetterQueueSpringRepository);
    this.deadLetterQueueEventSpringRepository = Assert.notNull(deadLetterQueueEventSpringRepository);
  }

  @Override
  public List<CloudEvent> findReintroduced(String feedConsumerName) {
    return deadLetterQueueSpringRepository.findTop1ByFeedConsumerNameAndAttemptReprocessingIsTrue(feedConsumerName)
      .stream()
      .map(this::mapFromEntityToShelvedTrace)
      .flatMap(trace -> trace.events().stream())
      .toList();
  }

  @Override
  public PagedList<ShelvedTrace> listTracesWithPagination(int page) {
    final Page<DeadLetterQueueEntity> pagedTraces = deadLetterQueueSpringRepository.findAllBy(PageRequest.of(page - 1, TRACES_LISTED_PER_PAGE));
    final List<ShelvedTrace> traces = pagedTraces
      .map(this::mapFromEntityToShelvedTrace)
      .toList();

    return new PagedList<>(traces, page, pagedTraces.getTotalPages(), pagedTraces.getTotalElements());
  }

  @Override
  public Optional<ShelvedTrace> checkTraceStatus(String traceId) {
    return deadLetterQueueSpringRepository.findById(traceId)
      .map(this::mapFromEntityToShelvedTrace);
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

        deadLetterQueueEventSpringRepository.save(dlqEvent);
      });
  }

  @Override
  public void mendEventData(String eventId, String serializedJsonData) {
    deadLetterQueueEventSpringRepository.findById(eventId)
      .ifPresent(dlqEvent -> {
        final CloudEvent cloudEvent = mapFromEntityToCloudEvent(dlqEvent);
        final Object updatedDomainEvent = domainEventSerializer.toDomainEvent(serializedJsonData);
        final CloudEvent updatedCloudEvent = CloudEvent.withUpdatedData(cloudEvent, updatedDomainEvent);

        final String reSerialized = cloudEventSerializer.toString(updatedCloudEvent);
        dlqEvent.setData(reSerialized);
        deadLetterQueueEventSpringRepository.save(dlqEvent);
      });
  }

  @Override
  public void reIntroduceForDelivery(String traceId) {
    deadLetterQueueSpringRepository.findById(traceId)
      .ifPresentOrElse(dlqTrace -> {
        dlqTrace.setAttemptReprocessing(true);
        deadLetterQueueSpringRepository.save(dlqTrace);
      }, () -> {
        throw new RuntimeException("No trace with ID " + traceId + " found");
      });
  }

  @Override
  public void reShelveAndUpdateCause(String traceId, String updatedError) {
    deadLetterQueueSpringRepository.findById(traceId)
      .ifPresent(dlqTrace -> {
        dlqTrace.setAttemptReprocessing(false);
        dlqTrace.setLastKnownError(updatedError);
        deadLetterQueueSpringRepository.save(dlqTrace);
      });
  }

  @Override
  public void markDelivered(String traceId, String eventId) {
    deadLetterQueueEventSpringRepository.deleteById(eventId);

    deadLetterQueueSpringRepository.findById(traceId)
      .ifPresent(dlqTrace -> {
        final Set<DeadLetterQueueEventEntity> events = dlqTrace.getEvents();

        events.removeIf(dlqEvent ->
          dlqEvent.getEventId().equals(eventId));
        deadLetterQueueSpringRepository.save(dlqTrace);

        if (events.isEmpty()) {
          deadLetterQueueSpringRepository.deleteById(traceId);
        }
      });
  }

  private ShelvedTrace mapFromEntityToShelvedTrace(DeadLetterQueueEntity entity) {
    return new ShelvedTrace(entity.getTraceId(), entity.getFeedConsumerName(), entity.getShelvedTime(), entity.getLastKnownError(), entity.isAttemptReprocessing(), entity.getEvents()
      .stream()
      .map(this::mapFromEntityToCloudEvent)
      .sorted(Comparator.comparing(CloudEvent::id))
      .toList());
  }

  private CloudEvent mapFromEntityToCloudEvent(DeadLetterQueueEventEntity entity) {
    return cloudEventDeserializer.toCloudEvent(entity.getData());
  }
}
