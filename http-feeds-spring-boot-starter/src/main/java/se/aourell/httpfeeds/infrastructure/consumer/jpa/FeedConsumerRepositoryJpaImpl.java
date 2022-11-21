package se.aourell.httpfeeds.infrastructure.consumer.jpa;

import se.aourell.httpfeeds.consumer.spi.FeedConsumerRepository;

import java.util.Optional;

public class FeedConsumerRepositoryJpaImpl implements FeedConsumerRepository {

  private final EventFeedConsummationSpringRepository eventFeedConsummationSpringRepository;

  public FeedConsumerRepositoryJpaImpl(EventFeedConsummationSpringRepository eventFeedConsummationSpringRepository) {
    this.eventFeedConsummationSpringRepository = eventFeedConsummationSpringRepository;
  }

  @Override
  public Optional<String> retrieveLastProcessedId(String feedName) {
    return eventFeedConsummationSpringRepository.findById(feedName)
      .map(EventFeedConsummation::getLastProcessedId);
  }

  @Override
  public void storeLastProcessedId(String feedName, String id) {
    final var eventFeedConsummation = eventFeedConsummationSpringRepository.findById(feedName)
      .orElseGet(() -> new EventFeedConsummation(feedName));

    eventFeedConsummation.setLastProcessedId(id);
    eventFeedConsummationSpringRepository.save(eventFeedConsummation);
  }
}
