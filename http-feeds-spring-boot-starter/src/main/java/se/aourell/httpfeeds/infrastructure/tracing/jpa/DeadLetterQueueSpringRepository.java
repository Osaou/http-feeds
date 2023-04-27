package se.aourell.httpfeeds.infrastructure.tracing.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DeadLetterQueueSpringRepository extends PagingAndSortingRepository<DeadLetterQueueEntity, String> {

  Page<DeadLetterQueueEntity> findAllBy(Pageable pageable);

  List<DeadLetterQueueEntity> findTop1ByFeedConsumerNameAndAttemptReprocessingIsTrue(String feedConsumerName);
}
