package se.aourell.httpfeeds.infrastructure.producer.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.producer.core.FeedItem;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;

import java.sql.Timestamp;
import java.util.List;

public class FeedItemRepositoryJdbcImpl implements FeedItemRepository {

  private final JdbcTemplate jdbcTemplate;
  private final FeedItemRowMapper feedItemRowMapper;

  private final String findAllSql;
  private final String findByIdGreaterThanSql;
  private final String findAllForSubjectSql;
  private final String findByIdGreaterThanForSubjectSql;
  private final String appendSql;

  public FeedItemRepositoryJdbcImpl(JdbcTemplate jdbcTemplate, FeedItemRowMapper feedItemRowMapper, String table, String feedName) {
    this.jdbcTemplate = jdbcTemplate;
    this.feedItemRowMapper = feedItemRowMapper;

    this.findAllSql = String.format("select id, type, feed_name, time, subject, method, data from %s where feed_name = '%s' order by id limit ?", table, feedName);
    this.findAllForSubjectSql = String.format("select id, type, feed_name, time, subject, method, data from %s where feed_name = '%s' and subject = ? order by id limit ?", table, feedName);

    this.findByIdGreaterThanSql = String.format("select id, type, feed_name, time, subject, method, data from %s where feed_name = '%s' and id > ? order by id limit ?", table, feedName);
    this.findByIdGreaterThanForSubjectSql = String.format("select id, type, feed_name, time, subject, method, data from %s where feed_name = '%s' and subject = ? and id > ? order by id limit ?", table, feedName);

    this.appendSql = String.format("insert into %s (id, type, feed_name, time, subject, method, data) values (?, ?, '%s', ?, ?, ?, ?)", table, feedName);
  }

  @Override
  public List<FeedItem> findAll(int limit) {
    return jdbcTemplate.query(findAllSql, feedItemRowMapper, limit);
  }

  @Override
  public List<FeedItem> findByIdGreaterThan(String lastEventId, int limit) {
    return jdbcTemplate.query(findByIdGreaterThanSql, feedItemRowMapper, lastEventId, limit);
  }

  @Override
  public List<FeedItem> findAllForSubject(String subject, int limit) {
    return jdbcTemplate.query(findAllForSubjectSql, feedItemRowMapper, subject, limit);
  }

  @Override
  public List<FeedItem> findByIdGreaterThanForSubject(String lastEventId, String subject, int limit) {
    return jdbcTemplate.query(findByIdGreaterThanForSubjectSql, feedItemRowMapper, subject, lastEventId, limit);
  }

  @Override
  public void append(FeedItem feedItem) {
    jdbcTemplate.update(appendSql, feedItem.id(), feedItem.type(), Timestamp.from(feedItem.time()), feedItem.subject(), feedItem.method(), feedItem.data());
  }
}
