package se.aourell.httpfeeds.infrastructure.consumer.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "eventfeeds_consummation")
public class FeedConsumerEntity {

  @Id
  @Column(name = "feed_name")
  private String feedName;

  @Column(name = "last_processed_id")
  private String lastProcessedId;

  public FeedConsumerEntity() { }

  public FeedConsumerEntity(String feedName) {
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
