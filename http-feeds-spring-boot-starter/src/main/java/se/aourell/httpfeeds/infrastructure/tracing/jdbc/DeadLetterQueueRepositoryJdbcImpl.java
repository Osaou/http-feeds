package se.aourell.httpfeeds.infrastructure.tracing.jdbc;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.tracing.core.ShelvedTrace;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;
import se.aourell.httpfeeds.util.PagedList;

import java.util.List;

public class DeadLetterQueueRepositoryJdbcImpl implements DeadLetterQueueRepository {

  @Override
  public PagedList<ShelvedTrace> listTracesWithPagination(int page) {
    return null;
  }

  @Override
  public List<ShelvedTrace> findReintroduced(String feedConsumerName) {
    return null;
  }

  @Override
  public boolean isTraceShelved(String traceId) {
    return false;
  }

  @Override
  public void shelveFromFeed(ShelvedTrace trace) {
  }

  @Override
  public void addEventToShelvedTrace(String traceId, CloudEvent event) {
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
