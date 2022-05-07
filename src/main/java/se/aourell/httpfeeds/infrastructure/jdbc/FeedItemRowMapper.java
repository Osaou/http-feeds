package se.aourell.httpfeeds.infrastructure.jdbc;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import se.aourell.httpfeeds.core.FeedItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Service
public class FeedItemRowMapper implements RowMapper<FeedItem> {

  @Override
  public FeedItem mapRow(ResultSet rs, int rowNum) throws SQLException {
    String id = rs.getString("id");
    String type = rs.getString("type");
    Timestamp time = rs.getTimestamp("time");
    String subject = rs.getString("subject");
    String method = rs.getString("method");
    String data = rs.getString("data");
    return new FeedItem(id, type, time == null ? null : time.toInstant(), subject, method, data);
  }
}
