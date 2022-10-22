package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.infrastructure.publisher.DomainEventSerializerImpl;
import se.aourell.httpfeeds.infrastructure.publisher.FeedItemIdGeneratorImpl;
import se.aourell.httpfeeds.infrastructure.publisher.FeedItemRepositoryImpl;
import se.aourell.httpfeeds.infrastructure.publisher.FeedItemRowMapper;
import se.aourell.httpfeeds.infrastructure.spring.http.HttpFeedsServerController;
import se.aourell.httpfeeds.publisher.core.CloudEventMapper;
import se.aourell.httpfeeds.publisher.core.EventFeedRegistryImpl;
import se.aourell.httpfeeds.publisher.spi.DomainEventSerializer;
import se.aourell.httpfeeds.publisher.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.publisher.spi.FeedItemRepositoryFactory;
import se.aourell.httpfeeds.publisher.spi.EventFeedRegistry;

@Configuration
@AutoConfigureAfter(ConsumerAutoConfiguration.class)
@EnableConfigurationProperties(ProducerProperties.class)
public class ProducerAutoConfiguration {

  @Bean
  public static BeanFactoryPostProcessor producerBeanFactoryPostProcessor(ProducerProperties producerProperties) {
    return new ProducerBeanFactoryPostProcessor(producerProperties);
  }

  @Bean
  @ConditionalOnMissingBean
  public EventFeedRegistry httpFeedRegistry() {
    return new EventFeedRegistryImpl();
  }

  @Bean
  @ConditionalOnMissingBean
  public FeedItemIdGenerator feedItemIdGenerator() {
    return new FeedItemIdGeneratorImpl();
  }

  @Bean
  @ConditionalOnMissingBean
  public DomainEventSerializer eventSerializer(@Qualifier("domainEventObjectMapper") ObjectMapper domainEventObjectMapper) {
    return new DomainEventSerializerImpl(domainEventObjectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  public CloudEventMapper cloudEventMapper(DomainEventSerializer domainEventSerializer) {
    return new CloudEventMapper(domainEventSerializer);
  }

  @Bean
  @ConditionalOnProperty(prefix = "eventfeeds.producer", name = "enabled", havingValue = "true")
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  public HttpFeedsServerController httpFeedsServerController(EventFeedRegistry feedRegistry, CloudEventMapper cloudEventMapper, @Qualifier("cloudEventObjectMapper") ObjectMapper cloudEventObjectMapper) {
    return new HttpFeedsServerController(feedRegistry, cloudEventMapper, cloudEventObjectMapper);
  }

  @Configuration
  @ConditionalOnClass(JdbcTemplate.class)
  public static class JdbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FeedItemRepositoryFactory feedItemRepositoryFactory(JdbcTemplate jdbcTemplate, FeedItemRowMapper feedItemRowMapper) {
      return (persistenceName, feedName) -> new FeedItemRepositoryImpl(jdbcTemplate, feedItemRowMapper, persistenceName, feedName);
    }

    @Bean
    @ConditionalOnMissingBean
    public FeedItemRowMapper feedItemRowMapper() {
      return new FeedItemRowMapper();
    }
  }
}
