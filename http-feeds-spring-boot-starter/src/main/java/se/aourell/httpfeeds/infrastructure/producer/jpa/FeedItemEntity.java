package se.aourell.httpfeeds.infrastructure.producer.jpa;

import se.aourell.httpfeeds.producer.core.FeedItem;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "eventfeeds")
public class FeedItemEntity {

  @Id
  private String id;
  private String type;
  private String feedName;
  private Instant time;
  private String subject;
  private String method;
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
