package se.aourell.httpfeeds.infrastructure.spring.autoconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import se.aourell.httpfeeds.core.CloudEvent;
import se.aourell.httpfeeds.core.HttpFeedRegistryImpl;
import se.aourell.httpfeeds.core.EventSerializerImpl;
import se.aourell.httpfeeds.infrastructure.spring.FeedItemRowMapper;
import se.aourell.httpfeeds.spi.EventSerializer;
import se.aourell.httpfeeds.spi.HttpFeedRegistry;

@Configuration
public class BaseAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public Jackson2ObjectMapperBuilder objectMapperBuilder() {
    return new Jackson2ObjectMapperBuilder()
      .failOnUnknownProperties(false)
      .serializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @Bean
  @ConditionalOnMissingBean
  public HttpFeedRegistry httpFeedRegistry() {
    return new HttpFeedRegistryImpl();
  }

  @Bean
  @ConditionalOnMissingBean
  public FeedItemRowMapper feedItemRowMapper() {
    return new FeedItemRowMapper();
  }

  @Bean
  @ConditionalOnMissingBean
  public EventSerializer eventSerializer(ObjectMapper objectMapper) {
    return new EventSerializerImpl(objectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  public CloudEvent.Mapper cloudEventMapper(EventSerializer eventSerializer) {
    return new CloudEvent.Mapper(eventSerializer);
  }
}
