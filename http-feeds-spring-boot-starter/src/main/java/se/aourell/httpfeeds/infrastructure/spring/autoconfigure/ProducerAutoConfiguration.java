package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.aourell.httpfeeds.infrastructure.producer.DomainEventSerializerImpl;
import se.aourell.httpfeeds.infrastructure.producer.FeedItemIdGeneratorImpl;
import se.aourell.httpfeeds.infrastructure.producer.creation.EventFeedCreatorImpl;
import se.aourell.httpfeeds.infrastructure.spring.http.HttpFeedsServerController;
import se.aourell.httpfeeds.producer.api.EventFeedCreator;
import se.aourell.httpfeeds.producer.core.CloudEventMapper;
import se.aourell.httpfeeds.producer.core.EventFeedRegistryImpl;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.producer.spi.EventFeedRegistry;
import se.aourell.httpfeeds.producer.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.producer.spi.FeedItemRepositoryFactory;
import se.aourell.httpfeeds.spi.ApplicationShutdownDetector;

@Configuration
@AutoConfigureAfter(ConsumerAutoConfiguration.class)
@EnableConfigurationProperties(ProducerProperties.class)
public class ProducerAutoConfiguration {

  @Bean
  public static BeanDefinitionRegistryPostProcessor producerBeanDefinitionRegistryPostProcessor() {
    return new ProducerBeanDefinitionRegistryPostProcessor();
  }

  @Bean
  public static BeanPostProcessor producerFeedItemServiceBeanPostProcessor(ProducerProperties producerProperties, EventFeedRegistry eventFeedRegistry) {
    return new ProducerFeedItemServiceBeanPostProcessor(producerProperties, eventFeedRegistry);
  }

  @Bean
  @ConditionalOnMissingBean
  public EventFeedCreator eventFeedCreator(DefaultListableBeanFactory defaultListableBeanFactory,
                                           ProducerProperties producerProperties,
                                           ApplicationShutdownDetector applicationShutdownDetector,
                                           FeedItemRepositoryFactory feedItemRepositoryFactory,
                                           FeedItemIdGenerator feedItemIdGenerator,
                                           DomainEventSerializer domainEventSerializer,
                                           EventFeedRegistry eventFeedRegistry) {
    return new EventFeedCreatorImpl(defaultListableBeanFactory, producerProperties, applicationShutdownDetector, feedItemRepositoryFactory, feedItemIdGenerator, domainEventSerializer, eventFeedRegistry);
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
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  public HttpFeedsServerController httpFeedsServerController(EventFeedRegistry feedRegistry, CloudEventMapper cloudEventMapper, @Qualifier("cloudEventObjectMapper") ObjectMapper cloudEventObjectMapper) {
    return new HttpFeedsServerController(feedRegistry, cloudEventMapper, cloudEventObjectMapper);
  }
}
