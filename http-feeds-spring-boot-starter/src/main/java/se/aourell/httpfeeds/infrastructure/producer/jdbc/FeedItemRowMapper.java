package se.aourell.httpfeeds.infrastructure.producer.jdbc;

import org.springframework.jdbc.core.RowMapper;
import se.aourell.httpfeeds.producer.core.FeedItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class FeedItemRowMapper implements RowMapper<FeedItem> {

  @Override
  public FeedItem mapRow(ResultSet rs, int rowNum) throws SQLException {
    String id = rs.getString(1);
    String type = rs.getString(2);
    String feedName = rs.getString(3);
    Timestamp time = rs.getTimestamp(4);
    String subject = rs.getString(5);
    String method = rs.getString(6);
    String data = rs.getString(7);
    return new FeedItem(id, type, feedName, time == null ? null : time.toInstant(), subject, method, data);
  }
}
