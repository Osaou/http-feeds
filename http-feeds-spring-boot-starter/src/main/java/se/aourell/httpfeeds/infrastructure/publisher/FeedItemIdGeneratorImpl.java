package se.aourell.httpfeeds.infrastructure.publisher;

import com.github.f4b6a3.uuid.UuidCreator;
import se.aourell.httpfeeds.publisher.spi.FeedItemIdGenerator;

public class FeedItemIdGeneratorImpl implements FeedItemIdGenerator {

  @Override
  public String generateId() {
    return UuidCreator.getTimeOrderedWithRandom().toString();
  }
}
