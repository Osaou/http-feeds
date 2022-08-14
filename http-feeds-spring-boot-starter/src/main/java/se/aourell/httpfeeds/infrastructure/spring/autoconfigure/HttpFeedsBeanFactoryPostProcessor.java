package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.atteo.classindex.ClassIndex;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.client.api.HttpFeedConsumer;
import se.aourell.httpfeeds.client.api.EventHandler;
import se.aourell.httpfeeds.client.core.FeedConsumerDefinition;
import se.aourell.httpfeeds.client.spi.FeedConsumerProcessor;
import se.aourell.httpfeeds.infrastructure.server.FeedItemRepositoryImpl;
import se.aourell.httpfeeds.infrastructure.server.FeedItemRowMapper;
import se.aourell.httpfeeds.server.api.HttpFeed;
import se.aourell.httpfeeds.server.core.EventBusImpl;
import se.aourell.httpfeeds.server.core.FeedItemServiceImpl;
import se.aourell.httpfeeds.server.spi.EventBus;
import se.aourell.httpfeeds.server.spi.EventSerializer;
import se.aourell.httpfeeds.server.spi.FeedItemIdGenerator;
import se.aourell.httpfeeds.server.spi.HttpFeedRegistry;

import java.util.Arrays;
import java.util.Objects;

public class HttpFeedsBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

  private final HttpFeedsServerProperties serverProperties;
  private final HttpFeedsClientProperties clientProperties;
  private final FeedConsumerProcessor feedConsumerProcessor;

  public HttpFeedsBeanFactoryPostProcessor(HttpFeedsServerProperties serverProperties, HttpFeedsClientProperties clientProperties, FeedConsumerProcessor feedConsumerProcessor) {
    this.serverProperties = serverProperties;
    this.clientProperties = clientProperties;
    this.feedConsumerProcessor = feedConsumerProcessor;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    initProducerBeans(beanFactory);
    initConsumerBeans(beanFactory);
  }

  private void initProducerBeans(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    final var jdbcTemplate = beanFactory.getBean(JdbcTemplate.class);
    final var feedItemRowMapper = beanFactory.getBean(FeedItemRowMapper.class);
    final var feedItemIdGenerator = beanFactory.getBean(FeedItemIdGenerator.class);
    final var feedRegistry = beanFactory.getBean(HttpFeedRegistry.class);
    final var eventSerializer = beanFactory.getBean(EventSerializer.class);
    final var beanClassLoader = Objects.requireNonNull(beanFactory.getBeanClassLoader());

    // find all annotated http feeds in domain code and add their configurations and necessary beans
    for (final Class<?> feedEventBaseType : ClassIndex.getAnnotated(HttpFeed.class, beanClassLoader)) {
      final var feedDeclaration = feedEventBaseType.getAnnotation(HttpFeed.class);
      final var feedName = feedDeclaration.feedName();
      final var persistenceName = feedDeclaration.persistenceName();

      // add bean for this event type's repository needs
      final var feedItemRepository = new FeedItemRepositoryImpl(jdbcTemplate, feedItemRowMapper, persistenceName);
      beanFactory.registerSingleton("repository:" + feedName, feedItemRepository);

      // add bean for this event type's service level needs
      final var feedItemService = new FeedItemServiceImpl(feedItemRepository, serverProperties.getPollInterval(), serverProperties.getLimit());
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

  private void initConsumerBeans(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    for (final var consumerBean : beanFactory.getBeansWithAnnotation(HttpFeedConsumer.class).values()) {
      final var feedName = consumerBean.getClass().getAnnotation(HttpFeedConsumer.class).feedName();
      final var feedUrl = clientProperties.getUrls().get(feedName);

      final var feedConsumer = feedConsumerProcessor.defineConsumer(feedName, feedUrl, consumerBean);
      initEventHandlersForConsumer(feedConsumer);
    }
  }

  private void initEventHandlersForConsumer(FeedConsumerDefinition consumerDefinition) {
    final var consumerBean = consumerDefinition.getBean();

    Arrays.stream(consumerBean.getClass().getDeclaredMethods())
      .filter(method -> Arrays.stream(method.getDeclaredAnnotations()).anyMatch(a -> a.annotationType() == EventHandler.class))
      .forEach(eventHandler -> {
        final var eventType = Arrays.stream(eventHandler.getParameterTypes())
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Event Handler methods must receive exactly 1 parameter, which is the event type it handles"));

        consumerDefinition.registerEventHandler(eventType, eventHandler);
      });
  }
}
