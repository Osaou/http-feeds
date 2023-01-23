package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import se.aourell.httpfeeds.consumer.api.FeedConsumerRegistry;
import se.aourell.httpfeeds.consumer.core.DisabledHttpFeedConsumerRegistryImpl;
import se.aourell.httpfeeds.consumer.core.HttpFeedConsumerRegistryImpl;
import se.aourell.httpfeeds.consumer.core.LocalFeedConsumerRegistryImpl;
import se.aourell.httpfeeds.consumer.spi.CloudEventArrayDeserializer;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepositoryFactory;
import se.aourell.httpfeeds.consumer.spi.HttpFeedConsumerRegistry;
import se.aourell.httpfeeds.consumer.spi.HttpFeedsClient;
import se.aourell.httpfeeds.consumer.spi.LocalFeedConsumerRegistry;
import se.aourell.httpfeeds.infrastructure.consumer.CloudEventArrayDeserializerImpl;
import se.aourell.httpfeeds.infrastructure.consumer.DomainEventDeserializerImpl;
import se.aourell.httpfeeds.infrastructure.consumer.HttpFeedsClientImpl;
import se.aourell.httpfeeds.producer.core.CloudEventMapper;
import se.aourell.httpfeeds.producer.spi.EventFeedRegistry;

@Configuration
@AutoConfigureAfter(AutoConfiguration.class)
@EnableConfigurationProperties(ConsumerProperties.class)
@EnableScheduling
public class ConsumerAutoConfiguration {

  private static ConsumerEventFeedConsumerBeanPostProcessor consumerEventFeedConsumerBeanPostProcessor;

  private static ConsumerEventFeedConsumerBeanPostProcessor consumerEventFeedConsumerBeanPostProcessor(ConsumerProperties consumerProperties, HttpFeedConsumerRegistry httpFeedConsumerRegistry, LocalFeedConsumerRegistry localFeedConsumerRegistry) {
    if (consumerEventFeedConsumerBeanPostProcessor == null) {
      consumerEventFeedConsumerBeanPostProcessor = new ConsumerEventFeedConsumerBeanPostProcessor(consumerProperties, httpFeedConsumerRegistry, localFeedConsumerRegistry);
    }
    return consumerEventFeedConsumerBeanPostProcessor;
  }

  @Bean
  public static BeanPostProcessor beanPostProcessor(ConsumerProperties consumerProperties, HttpFeedConsumerRegistry httpFeedConsumerRegistry, LocalFeedConsumerRegistry localFeedConsumerRegistry) {
    return consumerEventFeedConsumerBeanPostProcessor(consumerProperties, httpFeedConsumerRegistry, localFeedConsumerRegistry);
  }

  @Bean
  public FeedConsumerRegistry feedConsumerRegistry(ConsumerProperties consumerProperties, HttpFeedConsumerRegistry httpFeedConsumerRegistry, LocalFeedConsumerRegistry localFeedConsumerRegistry) {
    return consumerEventFeedConsumerBeanPostProcessor(consumerProperties, httpFeedConsumerRegistry, localFeedConsumerRegistry);
  }

  @Bean
  @ConditionalOnMissingBean
  public LocalFeedConsumerRegistry localFeedConsumerRegistry(DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository, EventFeedRegistry eventFeedRegistry, CloudEventMapper cloudEventMapper) {
    return new LocalFeedConsumerRegistryImpl(domainEventDeserializer, feedConsumerRepository, eventFeedRegistry, cloudEventMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  public DomainEventDeserializer domainEventDeserializer(@Qualifier("domainEventObjectMapper") ObjectMapper domainEventObjectMapper) {
    return new DomainEventDeserializerImpl(domainEventObjectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  public FeedConsumerRepository feedConsumerRepository(FeedConsumerRepositoryFactory feedConsumerRepositoryFactory, ConsumerProperties consumerProperties) {
    return feedConsumerRepositoryFactory.apply(consumerProperties.getTableName());
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
    public HttpFeedsClient httpFeedsClient(CloudEventArrayDeserializer cloudEventArrayDeserializer) {
      return new HttpFeedsClientImpl(cloudEventArrayDeserializer);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpFeedConsumerRegistry httpFeedConsumerRegistry(DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository, HttpFeedsClient httpFeedsClient) {
      return new HttpFeedConsumerRegistryImpl(domainEventDeserializer, feedConsumerRepository, httpFeedsClient);
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

  @Bean
  @ConditionalOnProperty(prefix = "eventfeeds.consumer", name = "enabled", havingValue = "true")
  public HttpFeedConsumerJob httpFeedConsumerJob(HttpFeedConsumerRegistry httpFeedConsumerRegistry) {
    return new HttpFeedConsumerJob(httpFeedConsumerRegistry);
  }

  static class HttpFeedConsumerJob {
    private final HttpFeedConsumerRegistry httpFeedConsumerRegistry;

    HttpFeedConsumerJob(HttpFeedConsumerRegistry httpFeedConsumerRegistry) {
      this.httpFeedConsumerRegistry = httpFeedConsumerRegistry;
    }

    @Scheduled(fixedDelay = 1)
    @Transactional
    public void consumeEvents() {
      httpFeedConsumerRegistry.batchPollAndProcessHttpFeedEvents();
    }
  }

  @Bean
  public LocalFeedConsumerJob localFeedConsumerJob(LocalFeedConsumerRegistry localFeedConsumerRegistry) {
    return new LocalFeedConsumerJob(localFeedConsumerRegistry);
  }

  static class LocalFeedConsumerJob {
    private final LocalFeedConsumerRegistry localFeedConsumerRegistry;

    LocalFeedConsumerJob(LocalFeedConsumerRegistry localFeedConsumerRegistry) {
      this.localFeedConsumerRegistry = localFeedConsumerRegistry;
    }

    @Scheduled(fixedDelay = 1)
    @Transactional
    public void consumeEvents() {
      localFeedConsumerRegistry.batchProcessLocalFeedEvents();
    }
  }
}
