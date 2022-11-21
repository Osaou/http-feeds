package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import se.aourell.httpfeeds.infrastructure.consumer.jpa.EventFeedConsummationSpringRepository;

@Configuration
@EntityScan(basePackageClasses = EventFeedConsummationSpringRepository.class)
@EnableJpaRepositories(basePackageClasses = EventFeedConsummationSpringRepository.class)
public class ConsumerJpaRepositoryAutoConfiguration { }
