package se.aourell.httpfeeds.infrastructure.consumer.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "eventfeeds_consummation")
public class EventFeedConsummation {

  @Id
  private String feedName;
  private String lastProcessedId;

  public EventFeedConsummation() { }

  public EventFeedConsummation(String feedName) {
    this.feedName = feedName;
  }

  public String getFeedName() {
    return feedName;
  }

  public void setFeedName(String feedName) {
    this.feedName = feedName;
  }

  public String getLastProcessedId() {
    return lastProcessedId;
  }

  public void setLastProcessedId(String lastProcessedId) {
    this.lastProcessedId = lastProcessedId;
  }
}
