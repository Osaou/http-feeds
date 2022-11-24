package se.aourell.httpfeeds.infrastructure.consumer.jpa;

import org.springframework.data.repository.CrudRepository;

public interface FeedConsumerSpringRepository extends CrudRepository<FeedConsumerEntity, String> { }
