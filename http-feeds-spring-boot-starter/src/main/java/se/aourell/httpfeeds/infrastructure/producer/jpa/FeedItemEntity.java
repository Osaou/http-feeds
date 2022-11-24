package se.aourell.httpfeeds.infrastructure.producer.jpa;

import se.aourell.httpfeeds.producer.core.FeedItem;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "eventfeeds")
public class FeedItemEntity {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "feed_name", nullable = false)
  private String feedName;

  @Column(name = "time", nullable = false)
  private Instant time;

  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "method")
  private String method;

  @Lob
  @Column(name = "data")
  private String data;

  public FeedItemEntity() { }

  public FeedItemEntity(FeedItem feedItem) {
    id = feedItem.id();
    type = feedItem.type();
    feedName = feedItem.feedName();
    time = feedItem.time();
    subject = feedItem.subject();
    method = feedItem.method();
    data = feedItem.data();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFeedName() {
    return feedName;
  }

  public void setFeedName(String feedName) {
    this.feedName = feedName;
  }

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
