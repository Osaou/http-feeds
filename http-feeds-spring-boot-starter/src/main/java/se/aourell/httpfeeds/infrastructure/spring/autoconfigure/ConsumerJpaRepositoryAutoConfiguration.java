package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import se.aourell.httpfeeds.infrastructure.consumer.jpa.FeedConsumerSpringRepository;

@Configuration
@EntityScan(basePackageClasses = FeedConsumerSpringRepository.class)
@EnableJpaRepositories(basePackageClasses = FeedConsumerSpringRepository.class)
public class ConsumerJpaRepositoryAutoConfiguration { }
