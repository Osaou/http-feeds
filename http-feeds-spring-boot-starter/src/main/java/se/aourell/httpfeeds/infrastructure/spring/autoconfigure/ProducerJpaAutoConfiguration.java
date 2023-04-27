package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import se.aourell.httpfeeds.infrastructure.producer.jpa.FeedItemSpringRepository;
import se.aourell.httpfeeds.infrastructure.producer.jpa.FeedItemRepositoryJpaImpl;
import se.aourell.httpfeeds.producer.spi.FeedItemRepositoryFactory;

import javax.persistence.EntityManager;

@Configuration
@ConditionalOnClass(EntityManager.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(TransactionAutoConfiguration.class)
@Import(ProducerJpaRepositoryAutoConfiguration.class)
public class ProducerJpaAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public FeedItemRepositoryFactory feedItemRepositoryFactory(FeedItemSpringRepository feedItemSpringRepository) {
    return (persistenceName, feedName) -> new FeedItemRepositoryJpaImpl(feedItemSpringRepository, feedName);
  }
}
