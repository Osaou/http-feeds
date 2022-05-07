package se.aourell.httpfeeds.core;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public class EventSerializer {

  private static final ObjectMapper objectMapper =
    Jackson2ObjectMapperBuilder.json()
      .failOnUnknownProperties(false)
      .serializationInclusion(Include.NON_NULL)
      .build();

  public static String toString(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static Object toEvent(String json) {
    try {
      return objectMapper.readValue(json, Object.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}