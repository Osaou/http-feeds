package se.aourell.httpfeeds.infrastructure.spring;

import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.core.FeedItem;
import se.aourell.httpfeeds.spi.FeedItemRepository;

import java.time.Instant;
import java.util.List;

public class FeedItemRepositoryImpl implements FeedItemRepository {

  private final JdbcTemplate jdbcTemplate;
  private final FeedItemRowMapper feedItemRowMapper;
  private final String table;
  private final String appendSql;
  private final String findAllSql;
  private final String findByIdGreaterThanSql;

  public FeedItemRepositoryImpl(JdbcTemplate jdbcTemplate, FeedItemRowMapper feedItemRowMapper, String table) {
    this.jdbcTemplate = jdbcTemplate;
    this.feedItemRowMapper = feedItemRowMapper;
    this.table = table;

    this.findAllSql = String.format("select * from %s order by id limit ?", table);
    this.findByIdGreaterThanSql = String.format("select * from %s where id > ? order by id limit ?", table);
    this.appendSql = String.format("insert into %s (id, type, time, subject, method, data) values (?, ?, ?, ?, ?, ?)", table);
  }

  @Override
  public List<FeedItem> findAll(long limit) {
    return jdbcTemplate.query(findAllSql, feedItemRowMapper, limit);
  }

  @Override
  public List<FeedItem> findByIdGreaterThan(String lastEventId, long limit) {
    return jdbcTemplate.query(findByIdGreaterThanSql, feedItemRowMapper, lastEventId, limit);
  }

  @Override
  public void append(String id, String type, Instant time, String subject, String method, String data) {
    jdbcTemplate.update(appendSql, id, type, time, subject, method, data);
  }

  public void deleteAll() {
    jdbcTemplate.update(String.format("delete from %s", table));
  }
}
