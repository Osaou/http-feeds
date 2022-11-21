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
import se.aourell.httpfeeds.infrastructure.producer.jdbc.FeedItemRepositoryJdbcImpl;
import se.aourell.httpfeeds.infrastructure.producer.jdbc.FeedItemRowMapper;
import se.aourell.httpfeeds.producer.spi.FeedItemRepositoryFactory;

@Configuration
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnMissingBean(FeedItemRepositoryFactory.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(TransactionAutoConfiguration.class)
public class ProducerJdbcAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public FeedItemRepositoryFactory feedItemRepositoryFactory(JdbcTemplate jdbcTemplate, FeedItemRowMapper feedItemRowMapper) {
    return (persistenceName, feedName) -> new FeedItemRepositoryJdbcImpl(jdbcTemplate, feedItemRowMapper, persistenceName, feedName);
  }

  @Bean
  @ConditionalOnMissingBean
  public FeedItemRowMapper feedItemRowMapper() {
    return new FeedItemRowMapper();
  }
}
