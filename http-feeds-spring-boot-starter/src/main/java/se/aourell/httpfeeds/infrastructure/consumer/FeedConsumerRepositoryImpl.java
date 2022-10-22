package se.aourell.httpfeeds.infrastructure.consumer;

import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;

import java.util.Optional;

public class FeedConsumerRepositoryImpl implements FeedConsumerRepository {

  public static final String DEFAULT_TABLE_NAME = "eventfeeds_consummation";

  private final JdbcTemplate jdbcTemplate;
  private final String retrieveSql;
  private final String storeSql;

  public FeedConsumerRepositoryImpl(JdbcTemplate jdbcTemplate, String table) {
    this.jdbcTemplate = jdbcTemplate;

    if (table == null || "".trim().equals(table)) {
      table = DEFAULT_TABLE_NAME;
    }

    this.retrieveSql = String.format("select lastProcessedId from %s where feedName = ?", table);
    this.storeSql = String.format("update %s set lastProcessedId = ? where feedName = ?", table);
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
