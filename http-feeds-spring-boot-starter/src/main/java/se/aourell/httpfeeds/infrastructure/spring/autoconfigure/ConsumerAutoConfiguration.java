package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import se.aourell.httpfeeds.consumer.core.DisabledHttpFeedConsumerRegistryImpl;
import se.aourell.httpfeeds.consumer.core.HttpFeedConsumerRegistryImpl;
import se.aourell.httpfeeds.consumer.core.LocalFeedConsumerRegistryImpl;
import se.aourell.httpfeeds.consumer.spi.CloudEventArrayDeserializer;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.HttpFeedConsumerRegistry;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepositoryFactory;
import se.aourell.httpfeeds.consumer.spi.HttpFeedsClient;
import se.aourell.httpfeeds.consumer.spi.LocalFeedConsumerRegistry;
import se.aourell.httpfeeds.infrastructure.consumer.CloudEventArrayDeserializerImpl;
import se.aourell.httpfeeds.infrastructure.consumer.DomainEventDeserializerImpl;
import se.aourell.httpfeeds.infrastructure.consumer.FeedConsumerRepositoryImpl;
import se.aourell.httpfeeds.infrastructure.consumer.HttpFeedsClientImpl;

@Configuration
@AutoConfigureAfter(AutoConfiguration.class)
@EnableConfigurationProperties(ConsumerProperties.class)
@EnableScheduling
public class ConsumerAutoConfiguration {

  @Bean
  public static BeanPostProcessor consumerBeanPostProcessor(ConsumerProperties consumerProperties, HttpFeedConsumerRegistry httpFeedConsumerRegistry, LocalFeedConsumerRegistry localFeedConsumerRegistry) {
    return new ConsumerBeanPostProcessor(consumerProperties, httpFeedConsumerRegistry, localFeedConsumerRegistry);
  }

  @Bean
  @ConditionalOnMissingBean
  public LocalFeedConsumerRegistry localFeedConsumerRegistry() {
    return new LocalFeedConsumerRegistryImpl();
  }

  @Configuration
  @ConditionalOnProperty(prefix = "eventfeeds.consumer", name = "enabled", havingValue = "true")
  public static class HttpFeedAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CloudEventArrayDeserializer cloudEventArrayDeserializer(@Qualifier("cloudEventObjectMapper") ObjectMapper cloudEventObjectMapper) {
      return new CloudEventArrayDeserializerImpl(cloudEventObjectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public DomainEventDeserializer eventDeserializer(@Qualifier("domainEventObjectMapper") ObjectMapper domainEventObjectMapper) {
      return new DomainEventDeserializerImpl(domainEventObjectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpFeedsClient httpFeedsClient(CloudEventArrayDeserializer cloudEventArrayDeserializer) {
      return new HttpFeedsClientImpl(cloudEventArrayDeserializer);
    }

    @Bean
    @ConditionalOnMissingBean
    public FeedConsumerRepository feedConsumerRepository(FeedConsumerRepositoryFactory feedConsumerRepositoryFactory, ConsumerProperties consumerProperties) {
      return feedConsumerRepositoryFactory.apply(consumerProperties.getTableName());
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpFeedConsumerRegistry httpFeedConsumerRegistry(HttpFeedsClient httpFeedsClient, DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository) {
      return new HttpFeedConsumerRegistryImpl(httpFeedsClient, domainEventDeserializer, feedConsumerRepository);
    }
  }

  @Configuration
  @ConditionalOnProperty(prefix = "eventfeeds.consumer", name = "enabled", havingValue = "false", matchIfMissing = true)
  public static class DisabledHttpFeedAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HttpFeedConsumerRegistry httpFeedConsumerRegistry() {
      return new DisabledHttpFeedConsumerRegistryImpl();
    }
  }

  @Configuration
  @ConditionalOnClass(JdbcTemplate.class)
  public static class JdbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FeedConsumerRepositoryFactory feedConsumerRepositoryFactory(JdbcTemplate jdbcTemplate) {
      return (persistenceName) -> new FeedConsumerRepositoryImpl(jdbcTemplate, persistenceName);
    }
  }

  @Bean
  @ConditionalOnProperty(prefix = "eventfeeds.consumer", name = "enabled", havingValue = "true")
  public FeedConsumerJob feedConsumerJob(HttpFeedConsumerRegistry httpFeedConsumerRegistry) {
    return new FeedConsumerJob(httpFeedConsumerRegistry);
  }

  static class FeedConsumerJob {
    private final HttpFeedConsumerRegistry httpFeedConsumerRegistry;

    FeedConsumerJob(HttpFeedConsumerRegistry httpFeedConsumerRegistry) {
      this.httpFeedConsumerRegistry = httpFeedConsumerRegistry;
    }

    @Scheduled(fixedDelay = 1)
    @Transactional
    public void consumeEvents() {
      httpFeedConsumerRegistry.batchPollAndProcessHttpFeedEvents();
    }
  }
}
