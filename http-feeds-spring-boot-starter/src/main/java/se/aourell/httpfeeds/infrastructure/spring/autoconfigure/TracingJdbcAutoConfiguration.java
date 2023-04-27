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
import se.aourell.httpfeeds.consumer.spi.CloudEventDeserializer;
import se.aourell.httpfeeds.infrastructure.tracing.jdbc.DeadLetterQueueRepositoryJdbcImpl;
import se.aourell.httpfeeds.producer.spi.CloudEventSerializer;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;

@Configuration
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnMissingBean(DeadLetterQueueRepository.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(TransactionAutoConfiguration.class)
public class TracingJdbcAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public DeadLetterQueueRepository deadLetterQueueRepository(JdbcTemplate jdbcTemplate, CloudEventSerializer cloudEventSerializer, CloudEventDeserializer cloudEventDeserializer) {
    return new DeadLetterQueueRepositoryJdbcImpl(jdbcTemplate, cloudEventSerializer, cloudEventDeserializer);
  }
}
