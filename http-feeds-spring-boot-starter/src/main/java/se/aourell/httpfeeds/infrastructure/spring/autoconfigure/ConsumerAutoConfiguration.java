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
import se.aourell.httpfeeds.consumer.api.EventFeedsConsumerApi;
import se.aourell.httpfeeds.consumer.core.LocalFeedFetcherImpl;
import se.aourell.httpfeeds.consumer.core.RemoteFeedFetcherImpl;
import se.aourell.httpfeeds.consumer.core.creation.EventFeedsConsumerApiImpl;
import se.aourell.httpfeeds.tracing.core.DeadLetterQueueService;
import se.aourell.httpfeeds.tracing.spi.ApplicationShutdownDetector;
import se.aourell.httpfeeds.consumer.spi.CloudEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepositoryFactory;
import se.aourell.httpfeeds.consumer.spi.HttpFeedsClient;
import se.aourell.httpfeeds.consumer.spi.LocalFeedFetcher;
import se.aourell.httpfeeds.consumer.spi.RemoteFeedFetcher;
import se.aourell.httpfeeds.infrastructure.consumer.CloudEventDeserializerImpl;
import se.aourell.httpfeeds.infrastructure.consumer.DomainEventDeserializerImpl;
import se.aourell.httpfeeds.consumer.core.HttpFeedsClientImpl;
import se.aourell.httpfeeds.producer.core.CloudEventMapper;
import se.aourell.httpfeeds.producer.spi.EventFeedsRegistry;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;

@Configuration
@AutoConfigureAfter(AutoConfiguration.class)
@EnableConfigurationProperties(ConsumerProperties.class)
@EnableScheduling
public class ConsumerAutoConfiguration {

  @Bean
  public static BeanPostProcessor beanPostProcessor(ConsumerProperties consumerProperties, EventFeedsConsumerApi eventFeedsConsumerApi) {
    return new ConsumerEventFeedConsumerBeanPostProcessor(consumerProperties, eventFeedsConsumerApi);
  }

  @Bean
  @ConditionalOnMissingBean
  public EventFeedsConsumerApi eventFeedsConsumerApi(ApplicationShutdownDetector applicationShutdownDetector,
                                                     LocalFeedFetcher localFeedFetcher,
                                                     RemoteFeedFetcher remoteFeedFetcher,
                                                     DomainEventDeserializer domainEventDeserializer,
                                                     FeedConsumerRepository feedConsumerRepository,
                                                     DeadLetterQueueService deadLetterQueueService,
                                                     DeadLetterQueueRepository deadLetterQueueRepository) {
    return new EventFeedsConsumerApiImpl(applicationShutdownDetector, localFeedFetcher, remoteFeedFetcher, domainEventDeserializer, feedConsumerRepository, deadLetterQueueService, deadLetterQueueRepository);
  }

  @Bean
  @ConditionalOnMissingBean
  public DeadLetterQueueService deadLetterQueueService(DeadLetterQueueRepository deadLetterQueueRepository) {
    return new DeadLetterQueueService(deadLetterQueueRepository);
  }

  @Bean
  @ConditionalOnMissingBean
  public LocalFeedFetcher localFeedFetcher(EventFeedsRegistry eventFeedsRegistry, CloudEventMapper cloudEventMapper) {
    return new LocalFeedFetcherImpl(eventFeedsRegistry, cloudEventMapper);
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
  public CloudEventDeserializer cloudEventDeserializer(@Qualifier("cloudEventObjectMapper") ObjectMapper cloudEventObjectMapper) {
    return new CloudEventDeserializerImpl(cloudEventObjectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  public HttpFeedsClient httpFeedsClient(CloudEventDeserializer cloudEventDeserializer) {
    return new HttpFeedsClientImpl(cloudEventDeserializer);
  }
}
