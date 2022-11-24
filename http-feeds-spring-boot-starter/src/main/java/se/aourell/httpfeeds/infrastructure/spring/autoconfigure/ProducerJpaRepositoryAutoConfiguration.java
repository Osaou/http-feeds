package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import se.aourell.httpfeeds.infrastructure.producer.jpa.FeedItemSpringRepository;

@Configuration
@EntityScan(basePackageClasses = FeedItemSpringRepository.class)
@EnableJpaRepositories(basePackageClasses = FeedItemSpringRepository.class)
public class ProducerJpaRepositoryAutoConfiguration { }
