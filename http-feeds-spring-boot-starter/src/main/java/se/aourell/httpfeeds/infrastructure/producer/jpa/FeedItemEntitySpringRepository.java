package se.aourell.httpfeeds.infrastructure.producer.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedItemEntitySpringRepository extends PagingAndSortingRepository<FeedItemEntity, String> {

  List<FeedItemEntity> findAllByFeedName(String feedName, Pageable pageable);

  List<FeedItemEntity> findAllByFeedNameAndIdGreaterThan(String feedName, String id, Pageable pageable);

  List<FeedItemEntity> findAllByFeedNameAndSubject(String feedName, String subject, Pageable pageable);

  List<FeedItemEntity> findAllByFeedNameAndIdGreaterThanAndSubject(String feedName, String lastEventId, String subject, Pageable pageable);
}
