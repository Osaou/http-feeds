package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.atteo.classindex.ClassIndex;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import se.aourell.httpfeeds.consumer.spi.LocalFeedConsumerRegistry;
import se.aourell.httpfeeds.publisher.api.EventFeed;
import se.aourell.httpfeeds.publisher.core.EventBusImpl;
import se.aourell.httpfeeds.publisher.core.EventFeedDefinition;
import se.aourell.httpfeeds.publisher.core.FeedItemServiceImpl;
import se.aourell.httpfeeds.publisher.spi.DomainEventSerializer;
import se.aourell.httpfeeds.publisher.spi.EventBus;
import se.aourell.httpfeeds.publisher.spi.EventFeedRegistry;
import se.aourell.httpfeeds.publisher.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.publisher.spi.FeedItemRepositoryFactory;

import java.util.Objects;

import static se.aourell.httpfeeds.publisher.spi.FeedItemRepository.DEFAULT_TABLE_NAME;

public class ProducerBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

  private final ProducerProperties producerProperties;

  public ProducerBeanFactoryPostProcessor(ProducerProperties producerProperties) {
    this.producerProperties = producerProperties;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    final var localFeedConsumerRegistry = beanFactory.getBean(LocalFeedConsumerRegistry.class);
    final var feedItemRepositoryFactory = beanFactory.getBean(FeedItemRepositoryFactory.class);
    final var feedItemIdGenerator = beanFactory.getBean(FeedItemIdGenerator.class);
    final var eventFeedRegistry = beanFactory.getBean(EventFeedRegistry.class);
    final var eventSerializer = beanFactory.getBean(DomainEventSerializer.class);
    final var beanClassLoader = Objects.requireNonNull(beanFactory.getBeanClassLoader());

    // find all annotated http feeds in domain code and add their configurations and necessary beans
    for (final Class<?> feedEventBaseType : ClassIndex.getAnnotated(EventFeed.class, beanClassLoader)) {
      final var feedDeclaration = feedEventBaseType.getAnnotation(EventFeed.class);
      final var feedName = EventFeedDefinition.validateFeedName(feedDeclaration.value());

      var persistenceName = feedDeclaration.persistenceName();
      if (persistenceName == null || "".equals(persistenceName.trim())) {
        persistenceName = DEFAULT_TABLE_NAME;
      }

      // add bean for this event type's repository needs
      final var feedItemRepository = feedItemRepositoryFactory.apply(persistenceName, feedName);
      beanFactory.registerSingleton("repository:" + feedName, feedItemRepository);

      // add bean for this event type's service level needs
      final var feedItemService = new FeedItemServiceImpl(feedItemRepository, producerProperties.getPollInterval(), producerProperties.getLimit());
      beanFactory.registerSingleton("service:" + feedName, feedItemService);

      // define the event feed in the registry, so that the http controller can read from it
      final var shouldPublish = producerProperties.shouldPublishHttpFeed(feedName);
      eventFeedRegistry.defineFeed(feedName, feedItemService, shouldPublish);

      // final piece of the puzzle: the eventbus that is scoped to this specific event type via generics
      final var eventBus = new EventBusImpl(feedEventBaseType, feedItemRepository, feedItemIdGenerator, eventSerializer, localFeedConsumerRegistry);
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
