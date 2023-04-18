package se.aourell.httpfeeds.infrastructure.producer.jpa;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import se.aourell.httpfeeds.producer.core.FeedItem;
import se.aourell.httpfeeds.producer.spi.FeedItemRepository;

import java.util.List;

public class FeedItemRepositoryJpaImpl implements FeedItemRepository {

  private final FeedItemSpringRepository feedItemSpringRepository;
  private final String feedName;

  public FeedItemRepositoryJpaImpl(FeedItemSpringRepository feedItemSpringRepository, String feedName) {
    this.feedItemSpringRepository = feedItemSpringRepository;
    this.feedName = feedName;
  }

  @Override
  public List<FeedItem> findAll(int limit) {
    final Sort sort = Sort.by(Sort.Direction.ASC, "id");
    final Pageable pageable = PageRequest.of(0, limit, sort);
    return feedItemSpringRepository.findAllByFeedName(feedName, pageable)
      .stream()
      .map(FeedItemRepositoryJpaImpl::mapFromEntityToFeedItem)
      .toList();
  }

  @Override
  public List<FeedItem> findByIdGreaterThan(String lastEventId, int limit) {
    final Sort sort = Sort.by(Sort.Direction.ASC, "id");
    final Pageable pageable = PageRequest.of(0, limit, sort);
    return feedItemSpringRepository.findAllByFeedNameAndIdGreaterThan(feedName, lastEventId, pageable)
      .stream()
      .map(FeedItemRepositoryJpaImpl::mapFromEntityToFeedItem)
      .toList();
  }

  @Override
  public List<FeedItem> findAllForSubject(String subject, int limit) {
    final Sort sort = Sort.by(Sort.Direction.ASC, "id");
    final Pageable pageable = PageRequest.of(0, limit, sort);
    return feedItemSpringRepository.findAllByFeedNameAndSubject(feedName, subject, pageable)
      .stream()
      .map(FeedItemRepositoryJpaImpl::mapFromEntityToFeedItem)
      .toList();
  }

  @Override
  public List<FeedItem> findByIdGreaterThanForSubject(String lastEventId, String subject, int limit) {
    final Sort sort = Sort.by(Sort.Direction.ASC, "id");
    final Pageable pageable = PageRequest.of(0, limit, sort);
    return feedItemSpringRepository.findAllByFeedNameAndIdGreaterThanAndSubject(feedName, lastEventId, subject, pageable)
      .stream()
      .map(FeedItemRepositoryJpaImpl::mapFromEntityToFeedItem)
      .toList();
  }

  @Override
  public void append(FeedItem feedItem) {
    final var entity = new FeedItemEntity(feedItem);
    feedItemSpringRepository.save(entity);
  }

  private static FeedItem mapFromEntityToFeedItem(FeedItemEntity entity) {
    return new FeedItem(entity.getId(), entity.getTraceId(), entity.getType(), entity.getTypeVersion(), entity.getFeedName(), entity.getTime(), entity.getSubject(), entity.getMethod(), entity.getData());
  }
}
