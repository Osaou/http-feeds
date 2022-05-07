package se.aourell.httpfeeds.infrastructure.spring;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.atteo.classindex.ClassIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.api.HttpFeed;
import se.aourell.httpfeeds.core.EventBusImpl;
import se.aourell.httpfeeds.core.HttpFeedRegistry;
import se.aourell.httpfeeds.core.HttpFeedRegistryImpl;
import se.aourell.httpfeeds.infrastructure.FeedServiceImpl;
import se.aourell.httpfeeds.spi.EventBus;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration(proxyBeanMethods = false)
public class SpringAutoConfiguration implements BeanFactoryPostProcessor {

  private static final Logger log = LoggerFactory.getLogger(SpringAutoConfiguration.class);
  private static final Duration pollInterval = Duration.of(1, ChronoUnit.SECONDS);
  private static final long limit = 1000;

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    log.info("In postProcessBeanFactory()");
    final var feedRegistry = beanFactory.getBean(HttpFeedRegistry.class);
    final var feedItemRowMapper = beanFactory.getBean(FeedItemRowMapper.class);

    // only set up controller automagically if we can resolve all necessary beans
    beanFactory
      .getBeansOfType(JdbcTemplate.class)
      .values().stream().findFirst()
      .ifPresent(jdbcTemplate -> {
        for (Class<?> feedEventBaseType : ClassIndex.getAnnotated(HttpFeed.class)) {
          final var feedDeclaration = feedEventBaseType.getAnnotation(HttpFeed.class);
          final var feedName = feedDeclaration.feed();
          final var table = feedDeclaration.table();

          final var repository = new FeedRepositoryImpl(jdbcTemplate, feedItemRowMapper, table);
          beanFactory.registerSingleton("repository:" + feedName, repository);

          final var resolvableType = ResolvableType.forClassWithGenerics(EventBus.class, feedEventBaseType);
          final var beanDefinition = new RootBeanDefinition();
          beanDefinition.setTargetType(resolvableType);
          beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
          beanDefinition.setAutowireCandidate(true);

          final var eventBus = new EventBusImpl(feedEventBaseType, repository);
          ((DefaultListableBeanFactory) beanFactory).registerBeanDefinition("eventbus:" + feedName, beanDefinition);
          beanFactory.registerSingleton("eventbus:" + feedName, eventBus);

          final var feedService = new FeedServiceImpl(repository, pollInterval, limit);
          feedRegistry.defineFeed(feedDeclaration, feedService);
        }
      });
  }

  @Bean
  @ConditionalOnMissingBean
  public Jackson2ObjectMapperBuilder objectMapperBuilder() {
    return new Jackson2ObjectMapperBuilder()
      .serializationInclusion(JsonInclude.Include.NON_NULL);
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
}
