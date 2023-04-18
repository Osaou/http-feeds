package se.aourell.httpfeeds.tracing.spi;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.tracing.core.ShelvedTrace;
import se.aourell.httpfeeds.util.PagedList;

import java.util.List;

public interface DeadLetterQueueRepository {

  PagedList<ShelvedTrace> listTracesWithPagination(int page);

  List<ShelvedTrace> findReintroduced(String feedConsumerName);

  boolean isTraceShelved(String traceId);

  void shelveFromFeed(ShelvedTrace trace);
  void addEventToShelvedTrace(String traceId, CloudEvent event);

  void reintroduceForDelivery(String traceId);

  void keepShelved(String traceId);
  void markDelivered(String traceId, String eventId);
}
