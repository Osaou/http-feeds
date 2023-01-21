package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import se.aourell.httpfeeds.consumer.api.EventFeedConsumer;
import se.aourell.httpfeeds.consumer.api.EventFeedConsumers;
import se.aourell.httpfeeds.consumer.api.EventHandler;
import se.aourell.httpfeeds.consumer.core.EventMetaData;
import se.aourell.httpfeeds.consumer.core.processing.FeedConsumerProcessor;
import se.aourell.httpfeeds.consumer.spi.HttpFeedConsumerRegistry;
import se.aourell.httpfeeds.consumer.spi.LocalFeedConsumerRegistry;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class  ConsumerEventFeedConsumerBeanPostProcessor implements BeanPostProcessor {

  private final ConsumerProperties consumerProperties;
  private final HttpFeedConsumerRegistry httpFeedConsumerRegistry;
  private final LocalFeedConsumerRegistry localFeedConsumerRegistry;

  public ConsumerEventFeedConsumerBeanPostProcessor(ConsumerProperties consumerProperties, HttpFeedConsumerRegistry httpFeedConsumerRegistry, LocalFeedConsumerRegistry localFeedConsumerRegistry) {
    this.consumerProperties = consumerProperties;
    this.httpFeedConsumerRegistry = httpFeedConsumerRegistry;
    this.localFeedConsumerRegistry = localFeedConsumerRegistry;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    final EventFeedConsumer feedConsumerDeclaration = bean.getClass().getAnnotation(EventFeedConsumer.class);
    if (feedConsumerDeclaration != null) {
      registerFeedConsumer(bean, feedConsumerDeclaration);
    }
    else {
      final EventFeedConsumers multipleDeclaration = bean.getClass().getAnnotation(EventFeedConsumers.class);
      Optional.ofNullable(multipleDeclaration)
        .map(EventFeedConsumers::value)
        .map(Stream::of)
        .ifPresent(consumers -> consumers.forEach(consumer -> registerFeedConsumer(bean, consumer)));
    }

    return bean;
  }

  private void registerFeedConsumer(Object bean, EventFeedConsumer feedConsumerDeclaration) {
    final String feedName = feedConsumerDeclaration.value();
    final String feedConsumerName = bean.getClass().getName() + ":" + feedName;
    final String baseUri = consumerProperties.getSources().get(feedName);

    final FeedConsumerProcessor processor = Optional.ofNullable(baseUri)
      .map(uri -> httpFeedConsumerRegistry.defineHttpFeedConsumer(feedConsumerName, bean, feedName, uri))
      .orElseGet(() -> localFeedConsumerRegistry.defineLocalConsumer(feedConsumerName, bean, feedName));

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
