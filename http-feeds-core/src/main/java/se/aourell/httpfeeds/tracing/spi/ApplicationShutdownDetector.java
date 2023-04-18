package se.aourell.httpfeeds.tracing.spi;

public interface ApplicationShutdownDetector {

  boolean isGracefulShutdown();
}
