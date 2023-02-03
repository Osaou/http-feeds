package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.aourell.httpfeeds.consumer.api.ConsumerGroupScheduler;
import se.aourell.httpfeeds.consumer.core.LocalFeedFetcherImpl;
import se.aourell.httpfeeds.consumer.core.RemoteFeedFetcherImpl;
import se.aourell.httpfeeds.consumer.core.creation.ConsumerGroupSchedulerImpl;
import se.aourell.httpfeeds.consumer.spi.CloudEventArrayDeserializer;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepositoryFactory;
import se.aourell.httpfeeds.consumer.spi.HttpFeedsClient;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;
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

  @Bean
  public static BeanPostProcessor beanPostProcessor(ConsumerProperties consumerProperties, ConsumerGroupScheduler consumerGroupScheduler) {
    return new ConsumerEventFeedConsumerBeanPostProcessor(consumerProperties, consumerGroupScheduler);
  }

  @Bean
  @ConditionalOnMissingBean
  public ConsumerGroupScheduler consumerGroupScheduler(LocalFeedFetcher localFeedFetcher, RemoteFeedFetcher remoteFeedFetcher, DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository) {
    return new ConsumerGroupSchedulerImpl(localFeedFetcher, remoteFeedFetcher, domainEventDeserializer, feedConsumerRepository);
  }

  @Bean
  @ConditionalOnMissingBean
  public LocalFeedFetcher localFeedFetcher(EventFeedRegistry eventFeedRegistry, CloudEventMapper cloudEventMapper) {
    return new LocalFeedFetcherImpl(eventFeedRegistry, cloudEventMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  public RemoteFeedFetcher remoteFeedFetcher(HttpFeedsClient httpFeedsClient) {
    return new RemoteFeedFetcherImpl(httpFeedsClient);
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
}
