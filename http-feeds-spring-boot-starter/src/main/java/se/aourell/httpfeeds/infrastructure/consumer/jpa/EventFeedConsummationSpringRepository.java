package se.aourell.httpfeeds.infrastructure.consumer.jpa;

import org.springframework.data.repository.CrudRepository;

public interface EventFeedConsummationSpringRepository extends CrudRepository<EventFeedConsummation, String> { }
