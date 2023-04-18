package se.aourell.httpfeeds;

import java.time.OffsetDateTime;
import java.util.Optional;

public record CloudEvent(
  /*
   * from http feeds spec
   */
  String specversion,
  String id,
  String type,
  String source,
  OffsetDateTime time,
  String subject,
  String method,
  String datacontenttype,
  Object data,

  /*
   * custom for us
   */
  Optional<String> traceId,
  Optional<Integer> typeVersion
) {

  public static final String DELETE_METHOD = "delete";

  public static CloudEvent of(String specversion,
                              String id,
                              String type,
                              String source,
                              OffsetDateTime time,
                              String subject,
                              String method,
                              String datacontenttype,
                              Object data,
                              String traceId,
                              int typeVersion) {
    return new CloudEvent(
      specversion,
      id,
      type,
      source,
      time,
      subject,
      method,
      datacontenttype,
      data,
      Optional.of(traceId),
      Optional.of(typeVersion)
    );
  }
}
