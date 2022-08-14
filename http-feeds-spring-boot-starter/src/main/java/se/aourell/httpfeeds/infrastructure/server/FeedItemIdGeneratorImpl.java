package se.aourell.httpfeeds.infrastructure.server;

import com.github.f4b6a3.uuid.UuidCreator;
import se.aourell.httpfeeds.server.spi.FeedItemIdGenerator;

public class FeedItemIdGeneratorImpl implements FeedItemIdGenerator {

  @Override
  public String generateId() {
    return UuidCreator.getTimeOrderedWithRandom().toString();
  }
}
