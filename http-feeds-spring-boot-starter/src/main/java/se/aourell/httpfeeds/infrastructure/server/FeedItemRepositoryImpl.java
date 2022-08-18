package se.aourell.httpfeeds.infrastructure.server;

import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.server.core.FeedItem;
import se.aourell.httpfeeds.server.spi.FeedItemRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class FeedItemRepositoryImpl implements FeedItemRepository {

  private static final String DEFAULT_TABLE_NAME = "httpfeeds";

  private final JdbcTemplate jdbcTemplate;
  private final FeedItemRowMapper feedItemRowMapper;
  private final String table;
  private final String appendSql;
  private final String findAllSql;
  private final String findByIdGreaterThanSql;

  public FeedItemRepositoryImpl(JdbcTemplate jdbcTemplate, FeedItemRowMapper feedItemRowMapper, String table, String source) {
    this.jdbcTemplate = jdbcTemplate;
    this.feedItemRowMapper = feedItemRowMapper;

    if (table == null || "".equals(table.trim())) {
      table = DEFAULT_TABLE_NAME;
    }
    this.table = table;

    this.findAllSql = String.format("select id, type, source, time, subject, method, data from %s where source = '%s' order by id limit ?", table, source);
    this.findByIdGreaterThanSql = String.format("select id, type, source, time, subject, method, data from %s where source = '%s' and id > ? order by id limit ?", table, source);
    this.appendSql = String.format("insert into %s (id, type, source, time, subject, method, data) values (?, ?, '%s', ?, ?, ?, ?)", table, source);
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
  public void append(String id, String type, Instant time, String subject, String method, String data) {
    jdbcTemplate.update(appendSql, id, type, Timestamp.from(time), subject, method, data);
  }

  public void deleteAll() {
    jdbcTemplate.update(String.format("delete from %s", table));
  }
}
