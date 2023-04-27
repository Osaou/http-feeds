package se.aourell.httpfeeds.infrastructure.tracing.jpa;

import org.springframework.data.repository.CrudRepository;

public interface DeadLetterQueueEventSpringRepository extends CrudRepository<DeadLetterQueueEventEntity, String> { }
