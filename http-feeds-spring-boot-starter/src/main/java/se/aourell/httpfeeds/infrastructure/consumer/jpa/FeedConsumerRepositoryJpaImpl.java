package se.aourell.httpfeeds.infrastructure.consumer.jpa;

import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;

import java.util.Optional;

public class FeedConsumerRepositoryJpaImpl implements FeedConsumerRepository {

  private final FeedConsumerSpringRepository feedConsumerSpringRepository;

  public FeedConsumerRepositoryJpaImpl(FeedConsumerSpringRepository feedConsumerSpringRepository) {
    this.feedConsumerSpringRepository = feedConsumerSpringRepository;
  }

  @Override
  public Optional<String> retrieveLastProcessedId(String feedName) {
    return feedConsumerSpringRepository.findById(feedName)
      .map(FeedConsumerEntity::getLastProcessedId);
  }

  @Override
  public void storeLastProcessedId(String feedName, String id) {
    final var eventFeedConsummation = feedConsumerSpringRepository.findById(feedName)
      .orElseGet(() -> new FeedConsumerEntity(feedName));

    eventFeedConsummation.setLastProcessedId(id);
    feedConsumerSpringRepository.save(eventFeedConsummation);
  }
}
