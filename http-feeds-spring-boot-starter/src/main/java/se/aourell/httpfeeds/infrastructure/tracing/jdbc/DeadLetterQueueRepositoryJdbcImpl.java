package se.aourell.httpfeeds.infrastructure.tracing.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.consumer.spi.CloudEventDeserializer;
import se.aourell.httpfeeds.producer.spi.CloudEventSerializer;
import se.aourell.httpfeeds.tracing.core.ShelvedTrace;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.util.PagedList;

import java.util.List;
import java.util.Optional;

public class DeadLetterQueueRepositoryJdbcImpl implements DeadLetterQueueRepository {

  private final JdbcTemplate jdbcTemplate;
  private final CloudEventSerializer cloudEventSerializer;
  private final CloudEventDeserializer cloudEventDeserializer;
  private final CloudEventRowMapper cloudEventRowMapper;
  private final ShelvedTraceRowMapper shelvedTraceRowMapper;

  private final String findReintroducedSql;
  private final String listTracesWithPagination;
  private final String isTraceShelvedSql;
  private final String shelveTraceSql;
  private final String shelveEventSql;

  public DeadLetterQueueRepositoryJdbcImpl(JdbcTemplate jdbcTemplate, CloudEventSerializer cloudEventSerializer, CloudEventDeserializer cloudEventDeserializer) {
    this.jdbcTemplate = jdbcTemplate;
    this.cloudEventSerializer = cloudEventSerializer;
    this.cloudEventDeserializer = cloudEventDeserializer;

    this.cloudEventRowMapper = new CloudEventRowMapper();
    this.shelvedTraceRowMapper = new ShelvedTraceRowMapper();

    this.findReintroducedSql = """
      select
        e.event_id, e.trace_id, e.data
      from eventfeeds_dlq_event e
      inner join eventfeeds_dlq q
        on q.trace_id = e.trace_id
      where
        q.feed_consumer_name = ?
        and q.attempt_reprocessing = 1
      """;

    this.listTracesWithPagination = """
      select
        q.trace_id, q.feed_consumer_name, q.shelved_time, q.last_known_error, ...
      from eventfeeds_dlq q
      inner join eventfeeds_dlq_event e
        on e.trace_id = q.trace_id
      limit ?
      offset ?
      """;

    this.isTraceShelvedSql = """
      select
        trace_id
      from eventfeeds_dlq
      where trace_id = ?
      """;

    this.shelveTraceSql = """
      insert into eventfeeds_dlq (trace_id, feed_consumer_name, shelved_time, last_known_error, attempt_reprocessing)
      values (?, ?, ?, ?, 0)
      """;

    this.shelveEventSql = """
      insert into eventfeeds_dlq_event (event_id, trace_id, data)
      values (?, ?, ?)
      """;
  }

  @Override
  public List<CloudEvent> findReintroduced(String feedConsumerName) {
    return jdbcTemplate.query(findReintroducedSql, cloudEventRowMapper, feedConsumerName);
  }

  @Override
  public PagedList<ShelvedTrace> listTracesWithPagination(int page) {
    return null;
  }

  @Override
  public Optional<ShelvedTrace> checkTraceStatus(String traceId) {
    return Optional.empty();
  }

  @Override
  public boolean isTraceShelved(String traceId) {
    return jdbcTemplate.queryForObject(isTraceShelvedSql, String.class, traceId) != null;
  }

  @Override
  public void shelveFromFeed(ShelvedTrace trace) {
    final String traceId = trace.traceId();
    jdbcTemplate.update(shelveTraceSql, traceId, trace.feedConsumerName(), trace.shelvedTime(), trace.lastKnownError());

    trace.events()
      .forEach(event -> {
        final String serializedEvent = cloudEventSerializer.toString(event);
        jdbcTemplate.update(shelveEventSql, event.id(), traceId, serializedEvent);
      });
  }

  @Override
  public void addEventToShelvedTrace(String traceId, CloudEvent event) {
    final String serializedEvent = cloudEventSerializer.toString(event);
    jdbcTemplate.update(shelveEventSql, event.id(), traceId, serializedEvent);
  }

  @Override
  public void mendEventData(String eventId, String serializedJsonData) {
  }

  @Override
  public void reintroduceForDelivery(String traceId) {
  }

  @Override
  public void keepShelved(String traceId) {
  }

  @Override
  public void markDelivered(String traceId, String eventId) {
  }
}
