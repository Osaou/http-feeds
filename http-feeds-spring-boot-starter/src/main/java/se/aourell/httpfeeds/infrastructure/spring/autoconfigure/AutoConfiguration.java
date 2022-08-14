package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "cloudEventObjectMapper")
  public ObjectMapper cloudEventObjectMapper() {
    return JsonMapper.builder()
      .serializationInclusion(JsonInclude.Include.NON_NULL)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .addModule(new JavaTimeModule())
      .build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "domainEventObjectMapper")
  public ObjectMapper domainEventObjectMapper() {
    return JsonMapper.builder()
      .serializationInclusion(JsonInclude.Include.NON_NULL)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .addModule(new JavaTimeModule())
      .build();
  }
}
