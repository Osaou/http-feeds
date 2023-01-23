package se.aourell.httpfeeds.infrastructure.consumer.jpa;

import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;

import java.util.Optional;

public class FeedConsumerRepositoryJpaImpl implements FeedConsumerRepository {

  private final FeedConsumerSpringRepository feedConsumerSpringRepository;

  public FeedConsumerRepositoryJpaImpl(FeedConsumerSpringRepository feedConsumerSpringRepository) {
    this.feedConsumerSpringRepository = feedConsumerSpringRepository;
  }

  @Override
  public Optional<String> retrieveLastProcessedId(String feedConsumerName) {
    return feedConsumerSpringRepository.findById(feedConsumerName)
      .map(FeedConsumerEntity::getLastProcessedId);
  }

  @Override
  public void storeLastProcessedId(String feedConsumerName, String id) {
    final var eventFeedConsummation = feedConsumerSpringRepository.findById(feedConsumerName)
      .orElseGet(() -> new FeedConsumerEntity(feedConsumerName));

    eventFeedConsummation.setLastProcessedId(id);
    feedConsumerSpringRepository.save(eventFeedConsummation);
  }
}
