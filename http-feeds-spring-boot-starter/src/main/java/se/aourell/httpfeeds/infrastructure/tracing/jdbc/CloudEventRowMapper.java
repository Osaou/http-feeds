package se.aourell.httpfeeds.infrastructure.tracing.jdbc;

import org.springframework.jdbc.core.RowMapper;
import se.aourell.httpfeeds.CloudEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class CloudEventRowMapper implements RowMapper<CloudEvent> {

  @Override
  public CloudEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
    final String id = rs.getString(1);
    final String traceId = rs.getString(2);
    final String type = rs.getString(3);
    final int typeVersion = rs.getInt(3);
    final String feedName = rs.getString(5);
    final Timestamp time = rs.getTimestamp(6);
    final String subject = rs.getString(7);
    final String method = rs.getString(8);
    final String data = rs.getString(9);
    return new CloudEvent(id, traceId, type, typeVersion, feedName, time == null ? null : OffsetDateTime.ofInstant(time.toInstant(), ZoneId.of("UTC")), subject, method, data);
  }
}
