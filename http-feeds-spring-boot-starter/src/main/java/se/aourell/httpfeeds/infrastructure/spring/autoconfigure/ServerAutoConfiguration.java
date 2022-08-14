package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.aourell.httpfeeds.infrastructure.server.DomainEventSerializerImpl;
import se.aourell.httpfeeds.infrastructure.server.FeedItemIdGeneratorImpl;
import se.aourell.httpfeeds.infrastructure.server.FeedItemRowMapper;
import se.aourell.httpfeeds.infrastructure.spring.http.HttpFeedsServerController;
import se.aourell.httpfeeds.server.core.CloudEventMapper;
import se.aourell.httpfeeds.server.core.HttpFeedRegistryImpl;
import se.aourell.httpfeeds.server.spi.DomainEventSerializer;
import se.aourell.httpfeeds.server.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.server.spi.HttpFeedRegistry;

@Configuration
@AutoConfigureAfter(AutoConfiguration.class)
@ConditionalOnProperty(prefix = "httpfeeds.server", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ServerProperties.class)
public class ServerAutoConfiguration {

  @Bean
  public ServerBeanFactoryPostProcessor serverBeanFactoryPostProcessor(ServerProperties serverProperties) {
    return new ServerBeanFactoryPostProcessor(serverProperties);
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
  public HttpFeedsServerController httpFeedsServerController(HttpFeedRegistry feedRegistry, CloudEventMapper cloudEventMapper, @Qualifier("cloudEventObjectMapper") ObjectMapper cloudEventObjectMapper) {
    return new HttpFeedsServerController(feedRegistry, cloudEventMapper, cloudEventObjectMapper);
  }
}
