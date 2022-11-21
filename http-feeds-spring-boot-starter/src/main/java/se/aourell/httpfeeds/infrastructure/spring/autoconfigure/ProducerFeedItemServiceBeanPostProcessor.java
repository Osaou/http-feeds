package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import se.aourell.httpfeeds.producer.spi.EventFeedRegistry;
import se.aourell.httpfeeds.producer.spi.FeedItemService;

public class ProducerFeedItemServiceBeanPostProcessor implements BeanPostProcessor {

  private final ProducerProperties producerProperties;
  private final EventFeedRegistry eventFeedRegistry;

  public ProducerFeedItemServiceBeanPostProcessor(ProducerProperties producerProperties, EventFeedRegistry eventFeedRegistry) {
    this.producerProperties = producerProperties;
    this.eventFeedRegistry = eventFeedRegistry;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof FeedItemService feedItemService) {
      final var feedName = beanName.substring("service:".length());
      final var shouldPublish = producerProperties.shouldPublishHttpFeed(feedName);
      eventFeedRegistry.defineFeed(feedName, feedItemService, shouldPublish);
    }

    return bean;
  }
}
