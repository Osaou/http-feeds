package se.aourell.httpfeeds.infrastructure.producer.creation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import se.aourell.httpfeeds.infrastructure.producer.FeedItemServiceImpl;
import se.aourell.httpfeeds.infrastructure.spring.autoconfigure.ProducerProperties;
import se.aourell.httpfeeds.producer.api.EventFeedsProducerApi;
import se.aourell.httpfeeds.producer.api.FeedAvailability;
import se.aourell.httpfeeds.producer.core.EventBusImpl;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.producer.spi.EventBus;
import se.aourell.httpfeeds.producer.spi.EventFeedRegistry;
import se.aourell.httpfeeds.producer.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;
import se.aourell.httpfeeds.producer.spi.FeedItemRepositoryFactory;
import se.aourell.httpfeeds.producer.spi.FeedItemService;
import se.aourell.httpfeeds.spi.ApplicationShutdownDetector;

public class EventFeedsProducerApiImpl implements EventFeedsProducerApi {

  private final DefaultListableBeanFactory defaultListableBeanFactory;
  private final ProducerProperties producerProperties;
  private final ApplicationShutdownDetector applicationShutdownDetector;
  private final FeedItemRepositoryFactory feedItemRepositoryFactory;
  private final FeedItemIdGenerator feedItemIdGenerator;
  private final DomainEventSerializer domainEventSerializer;
  private final EventFeedRegistry eventFeedRegistry;

  public EventFeedsProducerApiImpl(DefaultListableBeanFactory defaultListableBeanFactory,
                                   ProducerProperties producerProperties,
                                   ApplicationShutdownDetector applicationShutdownDetector,
                                   FeedItemRepositoryFactory feedItemRepositoryFactory,
                                   FeedItemIdGenerator feedItemIdGenerator,
                                   DomainEventSerializer domainEventSerializer,
                                   EventFeedRegistry eventFeedRegistry) {
    this.defaultListableBeanFactory = defaultListableBeanFactory;
    this.producerProperties = producerProperties;
    this.applicationShutdownDetector = applicationShutdownDetector;
    this.feedItemRepositoryFactory = feedItemRepositoryFactory;
    this.feedItemIdGenerator = feedItemIdGenerator;
    this.domainEventSerializer = domainEventSerializer;
    this.eventFeedRegistry = eventFeedRegistry;
  }

  @Override
  public <EventBaseType> EventBus<EventBaseType> publishEventFeed(String feedName, Class<EventBaseType> feedEventBaseType, FeedAvailability availability, String persistenceName) {
    // add bean for this event type's repository needs
    final FeedItemRepository feedItemRepository = feedItemRepositoryFactory.apply(persistenceName, feedName);
    final String repositoryBeanName = "repository:" + feedName;
    final var repositoryBeanDefinition = new RootBeanDefinition();
    repositoryBeanDefinition.setTargetType(FeedItemRepository.class);
    repositoryBeanDefinition.setInstanceSupplier(() -> feedItemRepository);
    repositoryBeanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    repositoryBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
    defaultListableBeanFactory.registerBeanDefinition(repositoryBeanName, repositoryBeanDefinition);

    // add bean for this event type's service level needs
    final FeedItemService feedItemService = new FeedItemServiceImpl(applicationShutdownDetector, feedItemRepository, producerProperties);
    final String serviceBeanName = "service:" + feedName;
    final var serviceBeanDefinition = new RootBeanDefinition();
    serviceBeanDefinition.setTargetType(FeedItemService.class);
    serviceBeanDefinition.setInstanceSupplier(() -> feedItemService);
    serviceBeanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    serviceBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
    serviceBeanDefinition.setDependsOn(repositoryBeanName);
    defaultListableBeanFactory.registerBeanDefinition(serviceBeanName, serviceBeanDefinition);

    // final piece of the puzzle: the eventbus that is scoped to this specific event type via generics
    final EventBus<EventBaseType> eventBus = new EventBusImpl<>(feedName, feedEventBaseType, feedItemRepository, feedItemIdGenerator, domainEventSerializer);
    final String eventBusBeanName = "eventbus:" + feedName;
    final var resolvableType = ResolvableType.forClassWithGenerics(EventBus.class, feedEventBaseType);
    final var eventBusBeanDefinition = new RootBeanDefinition();
    eventBusBeanDefinition.setTargetType(resolvableType);
    eventBusBeanDefinition.setInstanceSupplier(() -> eventBus);
    eventBusBeanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    eventBusBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
    eventBusBeanDefinition.setDependsOn(repositoryBeanName);
    defaultListableBeanFactory.registerBeanDefinition(eventBusBeanName, eventBusBeanDefinition);

    final boolean shouldPublish = switch (availability) {
      case PUBLISH_OVER_HTTP -> true;
      case APPLICATION_INTERNAL -> false;
    };
    eventFeedRegistry.defineFeed(feedName, feedItemService, shouldPublish);

    return eventBus;
  }
}
