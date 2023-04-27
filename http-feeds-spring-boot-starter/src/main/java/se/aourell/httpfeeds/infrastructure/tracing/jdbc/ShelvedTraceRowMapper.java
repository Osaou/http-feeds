package se.aourell.httpfeeds.infrastructure.tracing.jdbc;

import org.springframework.jdbc.core.RowMapper;
import se.aourell.httpfeeds.tracing.core.ShelvedTrace;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShelvedTraceRowMapper implements RowMapper<ShelvedTrace> {

  @Override
  public ShelvedTrace mapRow(ResultSet rs, int rowNum) throws SQLException {
    final String id = rs.getString(1);
    return new ShelvedTrace(id, );
  }
}
