package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import se.aourell.httpfeeds.infrastructure.producer.CloudEventSerializerImpl;
import se.aourell.httpfeeds.infrastructure.spring.TransactionContextImpl;
import se.aourell.httpfeeds.producer.core.CloudEventMapper;
import se.aourell.httpfeeds.producer.spi.CloudEventSerializer;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.tracing.spi.ApplicationShutdownDetector;
import se.aourell.httpfeeds.infrastructure.tracing.ApplicationShutdownDetectorImpl;
import se.aourell.httpfeeds.TransactionContext;

@Configuration
public class AutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ApplicationShutdownDetector applicationShutdownDetector() {
    return new ApplicationShutdownDetectorImpl();
  }

  @Bean
  @ConditionalOnMissingBean
  public TransactionContext transactionContext(PlatformTransactionManager platformTransactionManager) {
    return new TransactionContextImpl(platformTransactionManager);
  }

  @Bean
  @ConditionalOnMissingBean(name = "cloudEventObjectMapper")
  public ObjectMapper cloudEventObjectMapper() {
    return JsonMapper.builder()
      .serializationInclusion(JsonInclude.Include.NON_NULL)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .addModule(new JavaTimeModule())
      .addModule(new Jdk8Module())
      .build();
  }

  @Bean
  @ConditionalOnMissingBean(name = "domainEventObjectMapper")
  public ObjectMapper domainEventObjectMapper() {
    return JsonMapper.builder()
      .serializationInclusion(JsonInclude.Include.NON_NULL)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .addModule(new JavaTimeModule())
      .addModule(new Jdk8Module())
      .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public CloudEventMapper cloudEventMapper(DomainEventSerializer domainEventSerializer) {
    return new CloudEventMapper(domainEventSerializer);
  }

  @Bean
  @ConditionalOnMissingBean
  public CloudEventSerializer cloudEventSerializer(@Qualifier("cloudEventObjectMapper") ObjectMapper cloudEventObjectMapper) {
    return new CloudEventSerializerImpl(cloudEventObjectMapper);
  }
}
