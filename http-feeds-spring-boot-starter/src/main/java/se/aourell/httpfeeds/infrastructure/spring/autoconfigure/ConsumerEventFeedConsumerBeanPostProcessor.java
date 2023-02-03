package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import se.aourell.httpfeeds.consumer.api.ConsumerCreator;
import se.aourell.httpfeeds.consumer.api.ConsumerGroupCreator;
import se.aourell.httpfeeds.consumer.api.ConsumerGroupScheduler;
import se.aourell.httpfeeds.consumer.api.EventFeedConsumer;
import se.aourell.httpfeeds.consumer.api.EventFeedConsumers;
import se.aourell.httpfeeds.consumer.api.EventHandler;
import se.aourell.httpfeeds.consumer.core.EventMetaData;
import se.aourell.httpfeeds.producer.core.EventFeedDefinition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class ConsumerEventFeedConsumerBeanPostProcessor implements BeanPostProcessor {

  private final ConsumerProperties consumerProperties;
  private final ConsumerGroupScheduler consumerGroupScheduler;

  public ConsumerEventFeedConsumerBeanPostProcessor(ConsumerProperties consumerProperties, ConsumerGroupScheduler consumerGroupScheduler) {
    this.consumerProperties = consumerProperties;
    this.consumerGroupScheduler = consumerGroupScheduler;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    final EventFeedConsumer feedConsumerDeclaration = bean.getClass().getAnnotation(EventFeedConsumer.class);
    if (feedConsumerDeclaration != null) {
      consumerGroupScheduler.scheduleGroup(beanName, groupCreator -> defineAnnotatedConsumer(bean, feedConsumerDeclaration, groupCreator));
    } else {
      final EventFeedConsumers multipleDeclaration = bean.getClass().getAnnotation(EventFeedConsumers.class);
      Optional.ofNullable(multipleDeclaration)
        .map(EventFeedConsumers::value)
        .map(Stream::of)
        .ifPresent(consumers -> {
          consumerGroupScheduler.scheduleGroup(beanName, groupCreator -> consumers.forEach(consumer -> defineAnnotatedConsumer(bean, consumer, groupCreator)));
        });
    }

    return bean;
  }

  private void defineAnnotatedConsumer(Object bean, EventFeedConsumer feedConsumerDeclaration, ConsumerGroupCreator groupCreator) {
    final String feedName = feedConsumerDeclaration.value();
    final String feedConsumerName = bean.getClass().getName() + ":" + feedName;
    final String baseUri = consumerProperties.getSources().get(feedName);

    if (baseUri == null) {
      groupCreator.defineLocalConsumer(feedName, feedConsumerName, consumerCreator -> registerEventHandlersForBean(bean, consumerCreator));
    } else {
      final String feedUrl = EventFeedDefinition.fullUrlFromBaseUriAndFeedName(baseUri, feedName);
      groupCreator.defineRemoteConsumer(feedName, feedConsumerName, feedUrl, consumerCreator -> registerEventHandlersForBean(bean, consumerCreator));
    }
  }

  private void registerEventHandlersForBean(Object bean, ConsumerCreator consumerCreator) {
    // wire up event handlers for this bean
    findEventHandlersForConsumer(bean)
      .forEach(eventHandler -> {
        final Class<?>[] handlerParameters = eventHandler.getParameterTypes();
        final Class<?> eventType = handlerParameters[0];

        if (handlerParameters.length == 2) {
          consumerCreator.registerEventHandler(eventType, (event, metaData) -> {
            try {
              eventHandler.invoke(bean, event, metaData);
            } catch (IllegalAccessException | InvocationTargetException e) {
              throw new RuntimeException(e);
            }
          });
        } else {
          consumerCreator.registerEventHandler(eventType, (event) -> {
            try {
              eventHandler.invoke(bean, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
              throw new RuntimeException(e);
            }
          });
        }
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
