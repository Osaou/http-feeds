package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import se.aourell.httpfeeds.infrastructure.producer.jpa.FeedItemEntitySpringRepository;

@Configuration
@EntityScan(basePackageClasses = FeedItemEntitySpringRepository.class)
@EnableJpaRepositories(basePackageClasses = FeedItemEntitySpringRepository.class)
public class ProducerJpaRepositoryAutoConfiguration { }
