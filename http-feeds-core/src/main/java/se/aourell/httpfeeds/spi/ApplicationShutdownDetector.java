package se.aourell.httpfeeds.spi;

public interface ApplicationShutdownDetector {

  boolean isGracefulShutdown();
}
