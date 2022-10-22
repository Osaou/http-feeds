package se.aourell.httpfeeds;

import java.time.OffsetDateTime;

public record CloudEvent(String specversion, String id, String type, String source, OffsetDateTime time, String subject, String method, String datacontenttype, Object data) {

  public static final String DELETE_METHOD = "delete";
}
