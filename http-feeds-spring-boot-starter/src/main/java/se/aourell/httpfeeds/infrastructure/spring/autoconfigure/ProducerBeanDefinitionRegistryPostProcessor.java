package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.atteo.classindex.ClassIndex;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import se.aourell.httpfeeds.infrastructure.producer.FeedItemServiceImpl;
import se.aourell.httpfeeds.producer.api.EventFeed;
import se.aourell.httpfeeds.producer.core.EventBusImpl;
import se.aourell.httpfeeds.producer.core.EventFeedDefinition;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.producer.spi.EventBus;
import se.aourell.httpfeeds.producer.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;
import se.aourell.httpfeeds.producer.spi.FeedItemRepositoryFactory;
import se.aourell.httpfeeds.producer.spi.FeedItemService;
import se.aourell.httpfeeds.spi.ApplicationShutdownDetector;

import static se.aourell.httpfeeds.producer.spi.FeedItemRepository.DEFAULT_TABLE_NAME;

public class ProducerBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    // find all annotated http feeds in domain code and add their configurations and necessary beans
    for (final Class<?> feedEventBaseType : ClassIndex.getAnnotated(EventFeed.class)) {
      final var feedDeclaration = feedEventBaseType.getAnnotation(EventFeed.class);
      final var feedName = EventFeedDefinition.validateFeedName(feedDeclaration.value());

      String persistenceName = feedDeclaration.persistenceName();
      if (persistenceName == null || "".equals(persistenceName.trim())) {
        persistenceName = DEFAULT_TABLE_NAME;
      }

      // add bean for this event type's repository needs
      final String repositoryBeanName = "repository:" + feedName;
      final var repositoryBeanFactoryArguments = new ConstructorArgumentValues();
      repositoryBeanFactoryArguments.addIndexedArgumentValue(0, new RuntimeBeanReference(FeedItemRepositoryFactory.class));
      repositoryBeanFactoryArguments.addIndexedArgumentValue(1, persistenceName);
      repositoryBeanFactoryArguments.addIndexedArgumentValue(2, feedName);
      final var repositoryBeanDefinition = new RootBeanDefinition();
      repositoryBeanDefinition.setTargetType(FeedItemRepository.class);
      repositoryBeanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
      repositoryBeanDefinition.setFactoryBeanName("producerBeanDefinitionRegistryPostProcessor");
      repositoryBeanDefinition.setFactoryMethodName("createRepository");
      repositoryBeanDefinition.setConstructorArgumentValues(repositoryBeanFactoryArguments);
      repositoryBeanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
      repositoryBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
      registry.registerBeanDefinition(repositoryBeanName, repositoryBeanDefinition);

      // add bean for this event type's service level needs
      final String serviceBeanName = "service:" + feedName;
      final var serviceBeanArguments = new ConstructorArgumentValues();
      serviceBeanArguments.addIndexedArgumentValue(0, new RuntimeBeanReference(ApplicationShutdownDetector.class));
      serviceBeanArguments.addIndexedArgumentValue(1, new RuntimeBeanReference(repositoryBeanName));
      serviceBeanArguments.addIndexedArgumentValue(2, new RuntimeBeanReference(ProducerProperties.class));
      final var serviceBeanDefinition = new RootBeanDefinition();
      serviceBeanDefinition.setBeanClass(FeedItemServiceImpl.class);
      serviceBeanDefinition.setTargetType(FeedItemService.class);
      serviceBeanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
      serviceBeanDefinition.setConstructorArgumentValues(serviceBeanArguments);
      serviceBeanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
      serviceBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
      serviceBeanDefinition.setDependsOn(repositoryBeanName);
      registry.registerBeanDefinition(serviceBeanName, serviceBeanDefinition);

      // final piece of the puzzle: the eventbus that is scoped to this specific event type via generics
      final String eventBusBeanName = "eventbus:" + feedName;
      final var resolvableType = ResolvableType.forClassWithGenerics(EventBus.class, feedEventBaseType);
      final var eventBusBeanArguments = new ConstructorArgumentValues();
      eventBusBeanArguments.addIndexedArgumentValue(0, feedName);
      eventBusBeanArguments.addIndexedArgumentValue(1, feedEventBaseType);
      eventBusBeanArguments.addIndexedArgumentValue(2, new RuntimeBeanReference(repositoryBeanName));
      eventBusBeanArguments.addIndexedArgumentValue(3, new RuntimeBeanReference(FeedItemIdGenerator.class));
      eventBusBeanArguments.addIndexedArgumentValue(4, new RuntimeBeanReference(DomainEventSerializer.class));
      final var eventBusBeanDefinition = new RootBeanDefinition();
      eventBusBeanDefinition.setBeanClass(EventBusImpl.class);
      eventBusBeanDefinition.setTargetType(resolvableType);
      eventBusBeanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
      eventBusBeanDefinition.setConstructorArgumentValues(eventBusBeanArguments);
      eventBusBeanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
      eventBusBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
      eventBusBeanDefinition.setDependsOn(repositoryBeanName);
      registry.registerBeanDefinition(eventBusBeanName, eventBusBeanDefinition);
    }
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
  }

  public FeedItemRepository createRepository(FeedItemRepositoryFactory feedItemRepositoryFactory, String persistenceName, String feedName) {
    return feedItemRepositoryFactory.apply(persistenceName, feedName);
  }
}
