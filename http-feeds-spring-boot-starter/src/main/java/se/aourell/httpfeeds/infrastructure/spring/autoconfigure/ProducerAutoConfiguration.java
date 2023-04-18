package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.aourell.httpfeeds.infrastructure.producer.DomainEventSerializerImpl;
import se.aourell.httpfeeds.infrastructure.producer.FeedItemIdGeneratorImpl;
import se.aourell.httpfeeds.infrastructure.spring.http.HttpFeedsServerController;
import se.aourell.httpfeeds.producer.api.EventFeedsProducerApi;
import se.aourell.httpfeeds.producer.core.CloudEventMapper;
import se.aourell.httpfeeds.producer.core.EventFeedsRegistryImpl;
import se.aourell.httpfeeds.producer.core.creation.EventFeedsProducerApiImpl;
import se.aourell.httpfeeds.producer.spi.CloudEventSerializer;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.producer.spi.EventFeedsRegistry;
import se.aourell.httpfeeds.producer.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.producer.spi.FeedItemRepositoryFactory;
import se.aourell.httpfeeds.tracing.spi.ApplicationShutdownDetector;

@Configuration
@AutoConfigureAfter(ConsumerAutoConfiguration.class)
@EnableConfigurationProperties(ProducerProperties.class)
public class ProducerAutoConfiguration {

  @Bean
  public static BeanDefinitionRegistryPostProcessor producerBeanDefinitionRegistryPostProcessor() {
    return new ProducerBeanDefinitionRegistryPostProcessor();
  }

  @Bean
  @ConditionalOnMissingBean
  public EventFeedsProducerApi eventFeedsPublisherApi(ApplicationShutdownDetector applicationShutdownDetector,
                                                      FeedItemRepositoryFactory feedItemRepositoryFactory,
                                                      FeedItemIdGenerator feedItemIdGenerator,
                                                      DomainEventSerializer domainEventSerializer,
                                                      EventFeedsRegistry eventFeedsRegistry) {
    return new EventFeedsProducerApiImpl(applicationShutdownDetector, feedItemRepositoryFactory, feedItemIdGenerator, domainEventSerializer, eventFeedsRegistry);
  }

  @Bean
  @ConditionalOnMissingBean
  public EventFeedsRegistry eventFeedsRegistry() {
    return new EventFeedsRegistryImpl();
  }

  @Bean
  @ConditionalOnMissingBean
  public FeedItemIdGenerator feedItemIdGenerator() {
    return new FeedItemIdGeneratorImpl();
  }

  @Bean
  @ConditionalOnMissingBean
  public DomainEventSerializer domainEventSerializer(@Qualifier("domainEventObjectMapper") ObjectMapper domainEventObjectMapper) {
    return new DomainEventSerializerImpl(domainEventObjectMapper);
  }

  @Bean
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  public HttpFeedsServerController httpFeedsServerController(EventFeedsRegistry feedRegistry, CloudEventMapper cloudEventMapper, CloudEventSerializer cloudEventSerializer) {
    return new HttpFeedsServerController(feedRegistry, cloudEventMapper, cloudEventSerializer);
  }
}
