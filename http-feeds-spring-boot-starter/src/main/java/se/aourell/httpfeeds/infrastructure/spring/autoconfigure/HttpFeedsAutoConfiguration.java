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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import se.aourell.httpfeeds.client.core.FeedConsumerProcessorImpl;
import se.aourell.httpfeeds.client.spi.CloudEventArrayDeserializer;
import se.aourell.httpfeeds.client.spi.EventDeserializer;
import se.aourell.httpfeeds.client.spi.FeedConsumerProcessor;
import se.aourell.httpfeeds.client.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.client.spi.HttpFeedsClient;
import se.aourell.httpfeeds.infrastructure.client.CloudEventArrayDeserializerImpl;
import se.aourell.httpfeeds.infrastructure.client.EventDeserializerImpl;
import se.aourell.httpfeeds.infrastructure.client.FeedConsumerRepositoryImpl;
import se.aourell.httpfeeds.infrastructure.client.HttpFeedsClientImpl;
import se.aourell.httpfeeds.infrastructure.server.EventSerializerImpl;
import se.aourell.httpfeeds.infrastructure.server.FeedItemIdGeneratorImpl;
import se.aourell.httpfeeds.infrastructure.server.FeedItemRowMapper;
import se.aourell.httpfeeds.infrastructure.spring.http.HttpFeedsServerController;
import se.aourell.httpfeeds.server.core.CloudEventMapper;
import se.aourell.httpfeeds.server.core.HttpFeedRegistryImpl;
import se.aourell.httpfeeds.server.spi.EventSerializer;
import se.aourell.httpfeeds.server.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.server.spi.HttpFeedRegistry;

@Configuration
@EnableConfigurationProperties({HttpFeedsServerProperties.class, HttpFeedsClientProperties.class})
@EnableScheduling
public class HttpFeedsAutoConfiguration {

  @Bean
  public HttpFeedsBeanFactoryPostProcessor httpFeedsBeanFactoryPostProcessor(HttpFeedsServerProperties serverProperties, HttpFeedsClientProperties clientProperties, FeedConsumerProcessor feedConsumerProcessor) {
    return new HttpFeedsBeanFactoryPostProcessor(serverProperties, clientProperties, feedConsumerProcessor);
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

  /* server beans */

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
  public CloudEventMapper cloudEventMapper(EventSerializer eventSerializer) {
    return new CloudEventMapper(eventSerializer);
  }

  @Bean
  @ConditionalOnWebApplication(type = Type.SERVLET)
  @ConditionalOnProperty(prefix = "httpfeeds.server.rest", name = "enabled", matchIfMissing = false, havingValue = "true")
  public HttpFeedsServerController httpFeedsServerController(HttpFeedRegistry feedRegistry, CloudEventMapper cloudEventMapper, @Qualifier("cloudEventJsonMapper") ObjectMapper cloudEventJsonMapper) {
    return new HttpFeedsServerController(feedRegistry, cloudEventMapper, cloudEventJsonMapper);
  }

  /* client beans */

  @Bean
  @ConditionalOnMissingBean
  public CloudEventArrayDeserializer cloudEventArrayDeserializer(@Qualifier("cloudEventJsonMapper") ObjectMapper cloudEventJsonMapper) {
    return new CloudEventArrayDeserializerImpl(cloudEventJsonMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  public EventDeserializer eventDeserializer(@Qualifier("domainEventJsonMapper") ObjectMapper domainEventJsonMapper) {
    return new EventDeserializerImpl(domainEventJsonMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  public HttpFeedsClient httpFeedsClient(CloudEventArrayDeserializer cloudEventArrayDeserializer) {
    return new HttpFeedsClientImpl(cloudEventArrayDeserializer);
  }

  @Bean
  @ConditionalOnMissingBean
  public FeedConsumerRepository feedConsumerRepository(JdbcTemplate jdbcTemplate, HttpFeedsClientProperties clientProperties) {
    return new FeedConsumerRepositoryImpl(jdbcTemplate, clientProperties.getTableName());
  }

  @Bean
  @ConditionalOnMissingBean
  public FeedConsumerProcessor feedConsumerRegistry(HttpFeedsClient httpFeedsClient, EventDeserializer eventDeserializer, FeedConsumerRepository feedConsumerRepository) {
    return new FeedConsumerProcessorImpl(httpFeedsClient, eventDeserializer, feedConsumerRepository);
  }

  @Bean
  @ConditionalOnProperty(prefix = "httpfeeds.client.processor", name = "enabled", matchIfMissing = false, havingValue = "true")
  public FeedConsumerJob feedConsumerJob(FeedConsumerProcessor feedConsumerProcessor) {
    return new FeedConsumerJob(feedConsumerProcessor);
  }

  static class FeedConsumerJob {
    private final FeedConsumerProcessor feedConsumerProcessor;

    FeedConsumerJob(FeedConsumerProcessor feedConsumerProcessor) {
      this.feedConsumerProcessor = feedConsumerProcessor;
    }

    @Scheduled(fixedDelay = 100)
    @Transactional
    public void consumeEvents() {
      feedConsumerProcessor.batchPollAndProcessEvents();
    }
  }
}
