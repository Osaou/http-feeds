package se.aourell.httpfeeds.infrastructure.spring.autoconfig;

import org.atteo.classindex.ClassIndex;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.api.HttpFeed;
import se.aourell.httpfeeds.core.EventBusImpl;
import se.aourell.httpfeeds.core.FeedItemServiceImpl;
import se.aourell.httpfeeds.infrastructure.spring.FeedItemRepositoryImpl;
import se.aourell.httpfeeds.infrastructure.spring.FeedItemRowMapper;
import se.aourell.httpfeeds.spi.EventBus;
import se.aourell.httpfeeds.spi.EventSerializer;
import se.aourell.httpfeeds.spi.HttpFeedRegistry;

@Configuration
public class HttpFeedAutoConfiguration implements BeanFactoryPostProcessor {

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    final var jdbcTemplate = beanFactory.getBean(JdbcTemplate.class);
    final var feedItemRowMapper = beanFactory.getBean(FeedItemRowMapper.class);
    final var feedRegistry = beanFactory.getBean(HttpFeedRegistry.class);
    final var eventSerializer = beanFactory.getBean(EventSerializer.class);

    for (Class<?> feedEventBaseType : ClassIndex.getAnnotated(HttpFeed.class)) {
      final var feedDeclaration = feedEventBaseType.getAnnotation(HttpFeed.class);
      final var feedName = feedDeclaration.feedName();
      final var persistenceName = feedDeclaration.persistenceName();

      final var feedItemRepository = new FeedItemRepositoryImpl(jdbcTemplate, feedItemRowMapper, persistenceName);
      beanFactory.registerSingleton("repository:" + feedName, feedItemRepository);

      final var feedItemService = new FeedItemServiceImpl(feedItemRepository);
      beanFactory.registerSingleton("service:" + feedName, feedItemService);
      feedRegistry.defineFeed(feedDeclaration, feedItemService);

      final var eventBus = new EventBusImpl(feedEventBaseType, feedItemRepository, eventSerializer);
      final var resolvableType = ResolvableType.forClassWithGenerics(EventBus.class, feedEventBaseType);
      final var beanDefinition = new RootBeanDefinition();
      beanDefinition.setTargetType(resolvableType);
      beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
      beanDefinition.setAutowireCandidate(true);
      beanDefinition.setInstanceSupplier(() -> eventBus);
      ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition("eventbus:" + feedName, beanDefinition);
    }
  }
}
