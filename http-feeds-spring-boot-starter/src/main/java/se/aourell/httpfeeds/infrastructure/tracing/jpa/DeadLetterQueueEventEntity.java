package se.aourell.httpfeeds.infrastructure.tracing.jpa;

import se.aourell.httpfeeds.infrastructure.spring.JpaEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "eventfeeds_dlq_event")
public class DeadLetterQueueEventEntity extends JpaEntity<String> {

  @Id
  @Column(name = "event_id")
  private String eventId;

  @ManyToOne
  @JoinColumn(name = "trace_id", nullable = false)
  private DeadLetterQueueEntity trace;

  @Lob
  @Column(name = "data", nullable = false)
  private String data;

  @Override
  public String getId() {
    return getEventId();
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public DeadLetterQueueEntity getTrace() {
    return trace;
  }

  public void setTrace(DeadLetterQueueEntity trace) {
    this.trace = trace;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
