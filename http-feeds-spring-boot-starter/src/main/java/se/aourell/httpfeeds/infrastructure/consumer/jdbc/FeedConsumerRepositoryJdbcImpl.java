package se.aourell.httpfeeds.infrastructure.consumer.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;

import java.util.Optional;

public class FeedConsumerRepositoryJdbcImpl implements FeedConsumerRepository {

  public static final String DEFAULT_TABLE_NAME = "eventfeeds_consummation";

  private final JdbcTemplate jdbcTemplate;
  private final String retrieveSql;
  private final String storeSql;

  public FeedConsumerRepositoryJdbcImpl(JdbcTemplate jdbcTemplate, String table) {
    this.jdbcTemplate = jdbcTemplate;

    if (table == null || "".trim().equals(table)) {
      table = DEFAULT_TABLE_NAME;
    }

    this.retrieveSql = String.format("select last_processed_id from %s where feed_name = ?", table);
    this.storeSql = String.format("update %s set last_processed_id = ? where feed_name = ?", table);
  }

  @Override
  public Optional<String> retrieveLastProcessedId(String feedName) {
    return jdbcTemplate.query(retrieveSql, (rs, rowNum) -> rs.getString(1), feedName)
      .stream()
      .findFirst();
  }

  @Override
  public void storeLastProcessedId(String feedName, String id) {
    jdbcTemplate.update(storeSql, id, feedName);
  }
}
