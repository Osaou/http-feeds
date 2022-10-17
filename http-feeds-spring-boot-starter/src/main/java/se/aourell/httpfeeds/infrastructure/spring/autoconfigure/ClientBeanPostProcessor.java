package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import se.aourell.httpfeeds.client.api.EventHandler;
import se.aourell.httpfeeds.client.api.HttpFeedConsumer;
import se.aourell.httpfeeds.client.core.EventMetaData;
import se.aourell.httpfeeds.client.core.FeedConsumerDefinition;
import se.aourell.httpfeeds.client.spi.FeedConsumerProcessor;

import java.util.Arrays;

public class ClientBeanPostProcessor implements BeanPostProcessor {

  private final ClientProperties clientProperties;
  private final FeedConsumerProcessor feedConsumerProcessor;

  public ClientBeanPostProcessor(ClientProperties clientProperties, FeedConsumerProcessor feedConsumerProcessor) {
    this.clientProperties = clientProperties;
    this.feedConsumerProcessor = feedConsumerProcessor;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    final var feedConsumerDeclaration = bean.getClass().getAnnotation(HttpFeedConsumer.class);
    if (feedConsumerDeclaration != null) {
      String feedName = feedConsumerDeclaration.name();
      if (feedName == null || "".equals(feedName.trim())) {
        feedName = bean.getClass().getName();
      }

      final var feedUrl = clientProperties.getUrls().get(feedName);
      final var feedConsumer = feedConsumerProcessor.defineConsumer(feedName, feedUrl, bean);
      initEventHandlersForConsumer(feedConsumer);
    }

    return bean;
  }

  private void initEventHandlersForConsumer(FeedConsumerDefinition consumerDefinition) {
    final var consumerBean = consumerDefinition.getBean();

    // find all its methods annotated as event handlers
    Arrays.stream(consumerBean.getClass().getDeclaredMethods())
      .filter(method -> Arrays.stream(method.getDeclaredAnnotations()).anyMatch(a -> a.annotationType() == EventHandler.class))
      // only look at methods that take exactly 1 argument, or 2 if the second is of type metadata
      .filter(method -> {
        final var parameters = method.getParameterTypes();
        return parameters.length == 1 ||
          (parameters.length == 2 && parameters[1] == EventMetaData.class);
      })
      // wire up event handlers for this bean
      .forEach(eventHandler -> {
        final var eventType = eventHandler.getParameterTypes()[0];
        consumerDefinition.registerEventHandler(eventType, eventHandler);
      });
  }
}
