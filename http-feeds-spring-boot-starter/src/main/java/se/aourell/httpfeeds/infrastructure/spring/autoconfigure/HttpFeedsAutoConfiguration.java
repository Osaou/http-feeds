package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.aourell.httpfeeds.core.CloudEvent;
import se.aourell.httpfeeds.infrastructure.spring.EventSerializerImpl;
import se.aourell.httpfeeds.infrastructure.spring.FeedItemIdGeneratorImpl;
import se.aourell.httpfeeds.core.HttpFeedRegistryImpl;
import se.aourell.httpfeeds.infrastructure.spring.FeedItemRowMapper;
import se.aourell.httpfeeds.infrastructure.spring.HttpFeedsBeanFactoryPostProcessor;
import se.aourell.httpfeeds.infrastructure.spring.HttpFeedsController;
import se.aourell.httpfeeds.spi.EventSerializer;
import se.aourell.httpfeeds.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.spi.HttpFeedRegistry;

@Configuration
@EnableConfigurationProperties(HttpFeedsProperties.class)
public class HttpFeedsAutoConfiguration {

  @Bean
  public HttpFeedsBeanFactoryPostProcessor httpFeedsBeanFactoryPostProcessor(HttpFeedsProperties properties) {
    return new HttpFeedsBeanFactoryPostProcessor(properties);
  }

  @Bean
  @ConditionalOnMissingBean(name = "cloudEventJsonMapper")
  public ObjectMapper cloudEventJsonMapper() {
    return JsonMapper.builder()
      .serializationInclusion(JsonInclude.Include.NON_NULL)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .addModule(new JavaTimeModule())
      .build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "domainEventJsonMapper")
  public ObjectMapper domainEventJsonMapper() {
    return JsonMapper.builder()
      .serializationInclusion(JsonInclude.Include.NON_NULL)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .addModule(new JavaTimeModule())
      .build();
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
  public FeedItemIdGenerator feedItemIdGenerator() {
    return new FeedItemIdGeneratorImpl();
  }

  @Bean
  @ConditionalOnMissingBean
  public EventSerializer eventSerializer(@Qualifier("domainEventJsonMapper") ObjectMapper domainEventJsonMapper) {
    return new EventSerializerImpl(domainEventJsonMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  public CloudEvent.Mapper cloudEventMapper(EventSerializer eventSerializer) {
    return new CloudEvent.Mapper(eventSerializer);
  }

  @Bean
  @ConditionalOnWebApplication(type = Type.SERVLET)
  @ConditionalOnProperty(prefix = "httpfeeds.server.rest", name = "enabled", matchIfMissing = true, havingValue = "true")
  public HttpFeedsController httpFeedsController(HttpFeedRegistry feedRegistry, CloudEvent.Mapper cloudEventMapper, @Qualifier("cloudEventJsonMapper") ObjectMapper cloudEventJsonMapper) {
    return new HttpFeedsController(feedRegistry, cloudEventMapper, cloudEventJsonMapper);
  }
}
