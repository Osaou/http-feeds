package se.aourell.httpfeeds.infrastructure.tracing.jpa;

import se.aourell.httpfeeds.infrastructure.spring.JpaEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "eventfeeds_dlq")
public class DeadLetterQueueEntity extends JpaEntity<String> {

  @Id
  @Column(name = "trace_id", nullable = false)
  private String traceId;

  @Column(name = "feed_consumer_name", nullable = false)
  private String feedConsumerName;

  @Column(name = "shelved_time", nullable = false)
  private OffsetDateTime shelvedTime;

  @Lob
  @Column(name = "last_known_error", nullable = false)
  private String lastKnownError;

  @Column(name = "attempt_reprocessing", nullable = false)
  private boolean attemptReprocessing;

  @OneToMany(mappedBy = "trace", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Set<DeadLetterQueueEventEntity> events;

  @Override
  public String getId() {
    return getTraceId();
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  public String getFeedConsumerName() {
    return feedConsumerName;
  }

  public void setFeedConsumerName(String feedConsumerName) {
    this.feedConsumerName = feedConsumerName;
  }

  public OffsetDateTime getShelvedTime() {
    return shelvedTime;
  }

  public void setShelvedTime(OffsetDateTime shelvedTime) {
    this.shelvedTime = shelvedTime;
  }

  public String getLastKnownError() {
    return lastKnownError;
  }

  public void setLastKnownError(String lastKnownError) {
    this.lastKnownError = lastKnownError;
  }

  public boolean isAttemptReprocessing() {
    return attemptReprocessing;
  }

  public void setAttemptReprocessing(boolean attemptReprocessing) {
    this.attemptReprocessing = attemptReprocessing;
  }

  public Set<DeadLetterQueueEventEntity> getEvents() {
    return events;
  }

  public void setEvents(Set<DeadLetterQueueEventEntity> events) {
    this.events = events;
  }
}
