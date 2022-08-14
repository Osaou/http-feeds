package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import se.aourell.httpfeeds.client.core.FeedConsumerProcessorImpl;
import se.aourell.httpfeeds.client.spi.CloudEventArrayDeserializer;
import se.aourell.httpfeeds.client.spi.DomainEventDeserializer;
import se.aourell.httpfeeds.client.spi.FeedConsumerProcessor;
import se.aourell.httpfeeds.client.spi.FeedConsumerRepository;
import se.aourell.httpfeeds.client.spi.HttpFeedsClient;
import se.aourell.httpfeeds.infrastructure.client.CloudEventArrayDeserializerImpl;
import se.aourell.httpfeeds.infrastructure.client.DomainEventDeserializerImpl;
import se.aourell.httpfeeds.infrastructure.client.FeedConsumerRepositoryImpl;
import se.aourell.httpfeeds.infrastructure.client.HttpFeedsClientImpl;

@Configuration
@AutoConfigureAfter(AutoConfiguration.class)
@ConditionalOnProperty(prefix = "httpfeeds.client", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ClientProperties.class)
@EnableScheduling
public class ClientAutoConfiguration {

  @Bean
  public static ClientBeanFactoryPostProcessor clientBeanFactoryPostProcessor(ClientProperties clientProperties, FeedConsumerProcessor feedConsumerProcessor) {
    return new ClientBeanFactoryPostProcessor(clientProperties, feedConsumerProcessor);
  }

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
  public FeedConsumerRepository feedConsumerRepository(JdbcTemplate jdbcTemplate, ClientProperties clientProperties) {
    return new FeedConsumerRepositoryImpl(jdbcTemplate, clientProperties.getTableName());
  }

  @Bean
  @ConditionalOnMissingBean
  public FeedConsumerProcessor feedConsumerRegistry(HttpFeedsClient httpFeedsClient, DomainEventDeserializer domainEventDeserializer, FeedConsumerRepository feedConsumerRepository) {
    return new FeedConsumerProcessorImpl(httpFeedsClient, domainEventDeserializer, feedConsumerRepository);
  }

  @Bean
  public FeedConsumerJob feedConsumerJob(FeedConsumerProcessor feedConsumerProcessor) {
    return new FeedConsumerJob(feedConsumerProcessor);
  }

  static class FeedConsumerJob {
    private final FeedConsumerProcessor feedConsumerProcessor;

    FeedConsumerJob(FeedConsumerProcessor feedConsumerProcessor) {
      this.feedConsumerProcessor = feedConsumerProcessor;
    }

    @Scheduled(fixedDelay = 1)
    @Transactional
    public void consumeEvents() {
      feedConsumerProcessor.batchPollAndProcessEvents();
    }
  }
}
