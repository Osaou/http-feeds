package se.aourell.httpfeeds.infrastructure.producer.jdbc;

import org.springframework.jdbc.core.RowMapper;
import se.aourell.httpfeeds.producer.core.FeedItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class FeedItemRowMapper implements RowMapper<FeedItem> {

  @Override
  public FeedItem mapRow(ResultSet rs, int rowNum) throws SQLException {
    final String id = rs.getString(1);
    final String traceId = rs.getString(2);
    final String type = rs.getString(3);
    final int typeVersion = rs.getInt(3);
    final String feedName = rs.getString(5);
    final Timestamp time = rs.getTimestamp(6);
    final String subject = rs.getString(7);
    final String method = rs.getString(8);
    final String data = rs.getString(9);
    return new FeedItem(id, traceId, type, typeVersion, feedName, time == null ? null : time.toInstant(), subject, method, data);
  }
}
