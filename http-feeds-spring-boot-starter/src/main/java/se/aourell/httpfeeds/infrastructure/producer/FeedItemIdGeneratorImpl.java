package se.aourell.httpfeeds.infrastructure.producer;

import com.github.f4b6a3.uuid.UuidCreator;
import se.aourell.httpfeeds.producer.spi.FeedItemIdGenerator;

public class FeedItemIdGeneratorImpl implements FeedItemIdGenerator {

  @Override
  public String generateId() {
    return UuidCreator.getTimeOrderedEpoch().toString();
  }
}
