package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import se.aourell.httpfeeds.client.api.EventHandler;
import se.aourell.httpfeeds.client.api.HttpFeedConsumer;
import se.aourell.httpfeeds.client.core.FeedConsumerDefinition;
import se.aourell.httpfeeds.client.spi.FeedConsumerProcessor;

import java.util.Arrays;

public class ClientBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

  private final ClientProperties clientProperties;
  private final FeedConsumerProcessor feedConsumerProcessor;

  public ClientBeanFactoryPostProcessor(ClientProperties clientProperties, FeedConsumerProcessor feedConsumerProcessor) {
    this.clientProperties = clientProperties;
    this.feedConsumerProcessor = feedConsumerProcessor;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
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
