package se.aourell.httpfeeds.infrastructure.producer.jpa;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import se.aourell.httpfeeds.producer.core.FeedItem;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;

import java.util.List;

public class FeedItemRepositoryJpaImpl implements FeedItemRepository {

  private final FeedItemEntitySpringRepository feedItemEntitySpringRepository;
  private final String feedName;

  public FeedItemRepositoryJpaImpl(FeedItemEntitySpringRepository feedItemEntitySpringRepository, String feedName) {
    this.feedItemEntitySpringRepository = feedItemEntitySpringRepository;
    this.feedName = feedName;
  }

  @Override
  public List<FeedItem> findAll(int limit) {
    final Sort sort = Sort.by(Sort.Direction.ASC, "id");
    final Pageable pageable = PageRequest.of(0, limit, sort);
    return feedItemEntitySpringRepository.findAllByFeedName(feedName, pageable)
      .stream()
      .map(FeedItemRepositoryJpaImpl::mapFromEntityToFeedItem)
      .toList();
  }

  @Override
  public List<FeedItem> findByIdGreaterThan(String lastEventId, int limit) {
    final Sort sort = Sort.by(Sort.Direction.ASC, "id");
    final Pageable pageable = PageRequest.of(0, limit, sort);
    return feedItemEntitySpringRepository.findAllByFeedNameAndIdGreaterThan(feedName, lastEventId, pageable)
      .stream()
      .map(FeedItemRepositoryJpaImpl::mapFromEntityToFeedItem)
      .toList();
  }

  @Override
  public List<FeedItem> findAllForSubject(String subject, int limit) {
    final Sort sort = Sort.by(Sort.Direction.ASC, "id");
    final Pageable pageable = PageRequest.of(0, limit, sort);
    return feedItemEntitySpringRepository.findAllByFeedNameAndSubject(feedName, subject, pageable)
      .stream()
      .map(FeedItemRepositoryJpaImpl::mapFromEntityToFeedItem)
      .toList();
  }

  @Override
  public List<FeedItem> findByIdGreaterThanForSubject(String lastEventId, String subject, int limit) {
    final Sort sort = Sort.by(Sort.Direction.ASC, "id");
    final Pageable pageable = PageRequest.of(0, limit, sort);
    return feedItemEntitySpringRepository.findAllByFeedNameAndIdGreaterThanAndSubject(feedName, lastEventId, subject, pageable)
      .stream()
      .map(FeedItemRepositoryJpaImpl::mapFromEntityToFeedItem)
      .toList();
  }

  @Override
  public void append(FeedItem feedItem) {
    final var entity = new FeedItemEntity(feedItem);
    feedItemEntitySpringRepository.save(entity);
  }

  private static FeedItem mapFromEntityToFeedItem(FeedItemEntity entity) {
    return new FeedItem(entity.getId(), entity.getType(), entity.getFeedName(), entity.getTime(), entity.getSubject(), entity.getMethod(), entity.getData());
  }
}
