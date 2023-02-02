package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import se.aourell.httpfeeds.consumer.api.FeedConsumer;
import se.aourell.httpfeeds.consumer.api.FeedConsumerRegistry;
import se.aourell.httpfeeds.consumer.api.EventFeedConsumer;
import se.aourell.httpfeeds.consumer.api.EventFeedConsumers;
import se.aourell.httpfeeds.consumer.api.EventHandler;
import se.aourell.httpfeeds.consumer.core.EventMetaData;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.spi.HttpFeedConsumerRegistry;
import se.aourell.httpfeeds.consumer.spi.LocalFeedConsumerRegistry;
import se.aourell.httpfeeds.producer.core.EventFeedDefinition;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ConsumerEventFeedConsumerBeanPostProcessor implements BeanPostProcessor, FeedConsumerRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(ConsumerEventFeedConsumerBeanPostProcessor.class);

  private final ConsumerProperties consumerProperties;
  private final HttpFeedConsumerRegistry httpFeedConsumerRegistry;
  private final LocalFeedConsumerRegistry localFeedConsumerRegistry;

  private int manuallyDefinedConsumerIndex = 0;

  public ConsumerEventFeedConsumerBeanPostProcessor(ConsumerProperties consumerProperties, HttpFeedConsumerRegistry httpFeedConsumerRegistry, LocalFeedConsumerRegistry localFeedConsumerRegistry) {
    this.consumerProperties = consumerProperties;
    this.httpFeedConsumerRegistry = httpFeedConsumerRegistry;
    this.localFeedConsumerRegistry = localFeedConsumerRegistry;
  }

  @Override
  public FeedConsumer registerLocalConsumer(String feedName) {
    final String feedConsumerName = generateUniqueConsumerName(feedName);
    final FeedConsumer result = localFeedConsumerRegistry.defineLocalConsumer(feedConsumerName, null, feedName);

    LOG.debug("Registered Event Consumer (Local) for feed '{}' with unique name '{}'", feedName, feedConsumerName);
    return result;
  }

  @Override
  public FeedConsumer registerRemoteConsumer(String feedName) {
    final String baseUri = Objects.requireNonNull(consumerProperties.getSources().get(feedName));
    final String feedUrl = EventFeedDefinition.feedUrlFromName(baseUri, feedName);

    final String feedConsumerName = generateUniqueConsumerName(feedName);
    final FeedConsumer result = httpFeedConsumerRegistry.defineHttpFeedConsumer(feedConsumerName, null, feedName, feedUrl);

    LOG.debug("Registered Event Consumer (Remote) for feed '{}' with unique name '{}' for URL '{}'", feedName, feedConsumerName, feedUrl);
    return result;
  }

  @Override
  public FeedConsumer registerRemoteConsumer(String feedName, String feedUrl) {
    final String feedConsumerName = generateUniqueConsumerName(feedName);
    final FeedConsumer result = httpFeedConsumerRegistry.defineHttpFeedConsumer(feedConsumerName, null, feedName, feedUrl);

    LOG.debug("Registered Event Consumer (Remote) for feed '{}' with unique name '{}' for URL '{}'", feedName, feedConsumerName, feedUrl);
    return result;
  }

  private String generateUniqueConsumerName(String feedName) {
    ++manuallyDefinedConsumerIndex;
    return "feed-consumer:" + manuallyDefinedConsumerIndex + ":" + feedName;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    final EventFeedConsumer feedConsumerDeclaration = bean.getClass().getAnnotation(EventFeedConsumer.class);
    if (feedConsumerDeclaration != null) {
      registerAnnotatedFeedConsumer(bean, feedConsumerDeclaration);
    } else {
      final EventFeedConsumers multipleDeclaration = bean.getClass().getAnnotation(EventFeedConsumers.class);
      Optional.ofNullable(multipleDeclaration)
        .map(EventFeedConsumers::value)
        .map(Stream::of)
        .ifPresent(consumers -> consumers.forEach(consumer -> registerAnnotatedFeedConsumer(bean, consumer)));
    }

    return bean;
  }

  private void registerAnnotatedFeedConsumer(Object bean, EventFeedConsumer feedConsumerDeclaration) {
    final String feedName = feedConsumerDeclaration.value();
    final String feedConsumerName = bean.getClass().getName() + ":" + feedName;
    final String baseUri = consumerProperties.getSources().get(feedName);

    final FeedConsumerProcessor processor = Optional.ofNullable(baseUri)
      .map(uri -> {
        final String feedUrl = EventFeedDefinition.feedUrlFromName(uri, feedName);
        final FeedConsumerProcessor result = httpFeedConsumerRegistry.defineHttpFeedConsumer(feedConsumerName, bean, feedName, feedUrl);

        LOG.debug("Registered Event Consumer (Remote) [from annotation {}] for feed '{}' with unique name '{}' for URL '{}'",
          EventFeedConsumer.class.getName(),
          feedName,
          feedConsumerName,
          feedUrl);

        return result;
      })
      .orElseGet(() -> {
        final FeedConsumerProcessor result = localFeedConsumerRegistry.defineLocalConsumer(feedConsumerName, bean, feedName);

        LOG.debug("Registered Event Consumer (Local) [from annotation {}] for feed '{}' with unique name '{}'",
          EventFeedConsumer.class.getName(),
          feedName,
          feedConsumerName);

        return result;
      });

    // wire up event handlers for this bean
    findEventHandlersForConsumer(bean)
      .forEach(eventHandler -> {
        final Class<?> eventType = eventHandler.getParameterTypes()[0];
        processor.registerEventHandler(eventType, eventHandler);
      });
  }

  private Stream<Method> findEventHandlersForConsumer(Object bean) {
    // find all its methods annotated as event handlers
    return Arrays.stream(bean.getClass().getDeclaredMethods())
      .filter(method -> Arrays.stream(method.getDeclaredAnnotations()).anyMatch(a -> a.annotationType() == EventHandler.class))
      // only look at methods that take exactly 1 argument, or 2 if the second is of type metadata
      .filter(method -> {
        final Class<?>[] parameters = method.getParameterTypes();
        return parameters.length == 1 ||
          (parameters.length == 2 && parameters[1] == EventMetaData.class);
      });
  }
}
