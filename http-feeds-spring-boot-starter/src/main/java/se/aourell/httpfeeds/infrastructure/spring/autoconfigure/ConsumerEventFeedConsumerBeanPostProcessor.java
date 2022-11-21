package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import se.aourell.httpfeeds.consumer.api.EventFeedConsumer;
import se.aourell.httpfeeds.consumer.api.EventHandler;
import se.aourell.httpfeeds.consumer.core.EventMetaData;
import se.aourell.httpfeeds.consumer.spi.HttpFeedConsumerRegistry;
import se.aourell.httpfeeds.consumer.spi.LocalFeedConsumerRegistry;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ConsumerBeanPostProcessor implements BeanPostProcessor {

  private final ConsumerProperties consumerProperties;
  private final HttpFeedConsumerRegistry httpFeedConsumerRegistry;
  private final LocalFeedConsumerRegistry localFeedConsumerRegistry;

  public ConsumerBeanPostProcessor(ConsumerProperties consumerProperties, HttpFeedConsumerRegistry httpFeedConsumerRegistry, LocalFeedConsumerRegistry localFeedConsumerRegistry) {
    this.consumerProperties = consumerProperties;
    this.httpFeedConsumerRegistry = httpFeedConsumerRegistry;
    this.localFeedConsumerRegistry = localFeedConsumerRegistry;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    final var feedConsumerDeclaration = bean.getClass().getAnnotation(EventFeedConsumer.class);
    if (feedConsumerDeclaration != null) {
      String name = feedConsumerDeclaration.value();
      if (name == null || "".equals(name.trim())) {
        name = bean.getClass().getName();
      }

      final var feedName = name;
      final var baseUri = consumerProperties.getSources().get(feedName);
      final var httpFeedConsumer = Optional.ofNullable(baseUri).map(uri -> httpFeedConsumerRegistry.defineHttpFeedConsumer(feedName, uri, bean));
      final var localFeedConsumer = localFeedConsumerRegistry.defineLocalConsumer(feedName, bean);

      // wire up event handlers for this bean
      findEventHandlersForConsumer(bean)
        .forEach(eventHandler -> {
          final var eventType = eventHandler.getParameterTypes()[0];
          httpFeedConsumer.ifPresent(consumer -> consumer.registerEventHandler(eventType, eventHandler));
          localFeedConsumer.registerEventHandler(eventType, eventHandler);
        });
    }

    return bean;
  }

  private Stream<Method> findEventHandlersForConsumer(Object bean) {
    // find all its methods annotated as event handlers
    return Arrays.stream(bean.getClass().getDeclaredMethods())
      .filter(method -> Arrays.stream(method.getDeclaredAnnotations()).anyMatch(a -> a.annotationType() == EventHandler.class))
      // only look at methods that take exactly 1 argument, or 2 if the second is of type metadata
      .filter(method -> {
        final var parameters = method.getParameterTypes();
        return parameters.length == 1 ||
          (parameters.length == 2 && parameters[1] == EventMetaData.class);
      });
  }
}
