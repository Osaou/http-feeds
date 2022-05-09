package se.aourell.httpfeeds.infrastructure.spring;

import org.atteo.classindex.ClassIndex;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.api.HttpFeed;
import se.aourell.httpfeeds.core.EventBusImpl;
import se.aourell.httpfeeds.core.FeedItemServiceImpl;
import se.aourell.httpfeeds.infrastructure.spring.autoconfigure.HttpFeedsProperties;
import se.aourell.httpfeeds.spi.EventBus;
import se.aourell.httpfeeds.spi.EventSerializer;
import se.aourell.httpfeeds.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.spi.HttpFeedRegistry;

import java.util.Objects;

public class HttpFeedsBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

  private final HttpFeedsProperties properties;

  public HttpFeedsBeanFactoryPostProcessor(HttpFeedsProperties properties) {
    this.properties = properties;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    final var jdbcTemplate = beanFactory.getBean(JdbcTemplate.class);
    final var feedItemRowMapper = beanFactory.getBean(FeedItemRowMapper.class);
    final var feedItemIdGenerator = beanFactory.getBean(FeedItemIdGenerator.class);
    final var feedRegistry = beanFactory.getBean(HttpFeedRegistry.class);
    final var eventSerializer = beanFactory.getBean(EventSerializer.class);
    final var beanClassLoader = Objects.requireNonNull(beanFactory.getBeanClassLoader());

    // find all annotated http feeds in domain code and add their configurations and necessary beans
    for (Class<?> feedEventBaseType : ClassIndex.getAnnotated(HttpFeed.class, beanClassLoader)) {
      final var feedDeclaration = feedEventBaseType.getAnnotation(HttpFeed.class);
      final var feedName = feedDeclaration.feedName();
      final var persistenceName = feedDeclaration.persistenceName();

      // add bean for this event type's repository needs
      final var feedItemRepository = new FeedItemRepositoryImpl(jdbcTemplate, feedItemRowMapper, persistenceName);
      beanFactory.registerSingleton("repository:" + feedName, feedItemRepository);

      // add bean for this event type's service level needs
      final var feedItemService = new FeedItemServiceImpl(feedItemRepository, properties.getPollInterval(), properties.getLimit());
      beanFactory.registerSingleton("service:" + feedName, feedItemService);

      // define the http feed in the registry, so that the http controller can read from it
      final var feedDefinition = feedRegistry.defineFeed(feedDeclaration, feedItemService);

      // final piece of the puzzle: the eventbus that is scoped to this specific event type via generics
      final var eventBus = new EventBusImpl(feedEventBaseType, feedDefinition, feedItemRepository, feedItemIdGenerator, eventSerializer);
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
