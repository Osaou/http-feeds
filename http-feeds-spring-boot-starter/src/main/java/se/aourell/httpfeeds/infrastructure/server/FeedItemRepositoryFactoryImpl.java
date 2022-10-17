package se.aourell.httpfeeds.infrastructure.server;

import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.server.spi.FeedItemRepository;
import se.aourell.httpfeeds.server.spi.FeedItemRepositoryFactory;

public class FeedItemRepositoryFactoryImpl implements FeedItemRepositoryFactory {

  private final JdbcTemplate jdbcTemplate;
  private final FeedItemRowMapper feedItemRowMapper;

  public FeedItemRepositoryFactoryImpl(JdbcTemplate jdbcTemplate, FeedItemRowMapper feedItemRowMapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.feedItemRowMapper = feedItemRowMapper;
  }

  @Override
  public FeedItemRepository apply(String persistenceName, String feedPath) {
    return new FeedItemRepositoryImpl(jdbcTemplate, feedItemRowMapper, persistenceName, feedPath);
  }
}
