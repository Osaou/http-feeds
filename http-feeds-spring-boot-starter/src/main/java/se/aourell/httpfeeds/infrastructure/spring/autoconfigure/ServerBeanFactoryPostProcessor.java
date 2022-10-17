package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.atteo.classindex.ClassIndex;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import se.aourell.httpfeeds.server.api.HttpFeed;
import se.aourell.httpfeeds.server.core.EventBusImpl;
import se.aourell.httpfeeds.server.core.FeedItemServiceImpl;
import se.aourell.httpfeeds.server.spi.EventBus;
import se.aourell.httpfeeds.server.spi.DomainEventSerializer;
import se.aourell.httpfeeds.server.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.server.spi.FeedItemRepositoryFactory;
import se.aourell.httpfeeds.server.spi.HttpFeedRegistry;

import java.util.Objects;

import static se.aourell.httpfeeds.server.spi.FeedItemRepository.DEFAULT_TABLE_NAME;

public class ServerBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

  private final ServerProperties serverProperties;

  public ServerBeanFactoryPostProcessor(ServerProperties serverProperties) {
    this.serverProperties = serverProperties;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    final var feedItemRepositoryFactory = beanFactory.getBean(FeedItemRepositoryFactory.class);
    final var feedItemIdGenerator = beanFactory.getBean(FeedItemIdGenerator.class);
    final var feedRegistry = beanFactory.getBean(HttpFeedRegistry.class);
    final var eventSerializer = beanFactory.getBean(DomainEventSerializer.class);
    final var beanClassLoader = Objects.requireNonNull(beanFactory.getBeanClassLoader());

    // find all annotated http feeds in domain code and add their configurations and necessary beans
    for (final Class<?> feedEventBaseType : ClassIndex.getAnnotated(HttpFeed.class, beanClassLoader)) {
      final var feedDeclaration = feedEventBaseType.getAnnotation(HttpFeed.class);
      final var feedPath = feedRegistry.validateFeedPath(feedDeclaration.path());

      var persistenceName = feedDeclaration.persistenceName();
      if (persistenceName == null || "".equals(persistenceName.trim())) {
        persistenceName = DEFAULT_TABLE_NAME;
      }

      // add bean for this event type's repository needs
      final var feedItemRepository = feedItemRepositoryFactory.apply(persistenceName, feedPath);
      beanFactory.registerSingleton("repository:" + feedPath, feedItemRepository);

      // add bean for this event type's service level needs
      final var feedItemService = new FeedItemServiceImpl(feedItemRepository, serverProperties.getPollInterval(), serverProperties.getLimit());
      beanFactory.registerSingleton("service:" + feedPath, feedItemService);

      // define the http feed in the registry, so that the http controller can read from it
      feedRegistry.defineFeed(feedPath, feedItemService);

      // final piece of the puzzle: the eventbus that is scoped to this specific event type via generics
      final var eventBus = new EventBusImpl(feedEventBaseType, feedItemRepository, feedItemIdGenerator, eventSerializer);
      final var resolvableType = ResolvableType.forClassWithGenerics(EventBus.class, feedEventBaseType);
      final var beanDefinition = new RootBeanDefinition();
      beanDefinition.setTargetType(resolvableType);
      beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
      beanDefinition.setAutowireCandidate(true);
      beanDefinition.setInstanceSupplier(() -> eventBus);
      ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition("eventbus:" + feedPath, beanDefinition);
    }
  }
}
