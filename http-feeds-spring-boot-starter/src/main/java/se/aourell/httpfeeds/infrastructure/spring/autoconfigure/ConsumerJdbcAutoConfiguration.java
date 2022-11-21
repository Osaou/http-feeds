package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepositoryFactory;
import se.aourell.httpfeeds.infrastructure.consumer.jdbc.FeedConsumerRepositoryJdbcImpl;

@Configuration
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnMissingBean(FeedConsumerRepositoryFactory.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(TransactionAutoConfiguration.class)
public class ConsumerJdbcAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public FeedConsumerRepositoryFactory feedConsumerRepositoryFactory(JdbcTemplate jdbcTemplate) {
    return (persistenceName) -> new FeedConsumerRepositoryJdbcImpl(jdbcTemplate, persistenceName);
  }
}
