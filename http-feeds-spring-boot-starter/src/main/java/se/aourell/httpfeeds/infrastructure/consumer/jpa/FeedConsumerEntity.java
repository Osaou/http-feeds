package se.aourell.httpfeeds.infrastructure.consumer.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = FeedConsumerEntity.TABLE_NAME)
public class FeedConsumerEntity {

  public static final String TABLE_NAME = "eventfeeds_processed";

  @Id
  @Column(name = "feed_consumer_name")
  private String feedConsumerName;

  @Column(name = "last_processed_id")
  private String lastProcessedId;

  public FeedConsumerEntity() { }

  public FeedConsumerEntity(String feedConsumerName) {
    this.feedConsumerName = feedConsumerName;
  }

  public String getFeedConsumerName() {
    return feedConsumerName;
  }

  public void setFeedConsumerName(String feedConsumerName) {
    this.feedConsumerName = feedConsumerName;
  }

  public String getLastProcessedId() {
    return lastProcessedId;
  }

  public void setLastProcessedId(String lastProcessedId) {
    this.lastProcessedId = lastProcessedId;
  }
}
