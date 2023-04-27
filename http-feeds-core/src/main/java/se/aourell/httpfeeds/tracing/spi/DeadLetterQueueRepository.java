package se.aourell.httpfeeds.tracing.spi;

import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.tracing.core.ShelvedTrace;
import se.aourell.httpfeeds.util.PagedList;

import java.util.List;
import java.util.Optional;

public interface DeadLetterQueueRepository {

  List<CloudEvent> findReintroduced(String feedConsumerName);
  PagedList<ShelvedTrace> listTracesWithPagination(int page);
  Optional<ShelvedTrace> checkTraceStatus(String traceId);

  boolean isTraceShelved(String traceId);

  void shelveFromFeed(ShelvedTrace trace);
  void addEventToShelvedTrace(String traceId, CloudEvent event);

  void mendEventData(String eventId, String serializedJsonData);
  void reintroduceForDelivery(String traceId);

  void keepShelved(String traceId);
  void markDelivered(String traceId, String eventId);
}
