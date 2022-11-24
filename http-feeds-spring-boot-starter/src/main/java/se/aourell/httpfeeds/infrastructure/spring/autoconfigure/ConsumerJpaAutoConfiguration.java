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
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepositoryFactory;
import se.aourell.httpfeeds.infrastructure.consumer.jpa.FeedConsumerSpringRepository;
import se.aourell.httpfeeds.infrastructure.consumer.jpa.FeedConsumerRepositoryJpaImpl;

import javax.persistence.EntityManager;

@Configuration
@ConditionalOnClass(EntityManager.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore({ TransactionAutoConfiguration.class, ConsumerJdbcAutoConfiguration.class })
@Import(ConsumerJpaRepositoryAutoConfiguration.class)
public class ConsumerJpaAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public FeedConsumerRepositoryFactory feedConsumerRepositoryFactory(FeedConsumerSpringRepository feedConsumerSpringRepository) {
    return (persistenceName) -> new FeedConsumerRepositoryJpaImpl(feedConsumerSpringRepository);
  }
}
