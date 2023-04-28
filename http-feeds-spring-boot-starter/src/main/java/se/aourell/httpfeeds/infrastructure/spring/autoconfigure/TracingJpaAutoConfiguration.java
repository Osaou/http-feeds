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
import se.aourell.httpfeeds.consumer.spi.CloudEventDeserializer;
import se.aourell.httpfeeds.infrastructure.tracing.jpa.DeadLetterQueueEventSpringRepository;
import se.aourell.httpfeeds.infrastructure.tracing.jpa.DeadLetterQueueRepositoryJpaImpl;
import se.aourell.httpfeeds.infrastructure.tracing.jpa.DeadLetterQueueSpringRepository;
import se.aourell.httpfeeds.producer.spi.CloudEventSerializer;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;

import javax.persistence.EntityManager;

@Configuration
@ConditionalOnClass(EntityManager.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(TransactionAutoConfiguration.class)
@Import(TracingJpaRepositoryAutoConfiguration.class)
public class TracingJpaAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public DeadLetterQueueRepository deadLetterQueueRepository(CloudEventSerializer cloudEventSerializer,
                                                             CloudEventDeserializer cloudEventDeserializer,
                                                             DomainEventSerializer domainEventSerializer,
                                                             DeadLetterQueueSpringRepository deadLetterQueueSpringRepository,
                                                             DeadLetterQueueEventSpringRepository deadLetterQueueEventSpringRepository) {
    return new DeadLetterQueueRepositoryJpaImpl(cloudEventSerializer, cloudEventDeserializer, domainEventSerializer, deadLetterQueueSpringRepository, deadLetterQueueEventSpringRepository);
  }
}
