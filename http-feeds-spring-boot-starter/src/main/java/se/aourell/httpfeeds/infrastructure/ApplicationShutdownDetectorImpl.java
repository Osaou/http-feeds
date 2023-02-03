package se.aourell.httpfeeds.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.spi.ApplicationShutdownDetector;

public class ApplicationShutdownDetectorImpl implements ApplicationShutdownDetector {

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationShutdownDetectorImpl.class);

  private volatile boolean isApplicationShuttingDown = false;

  public ApplicationShutdownDetectorImpl() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      isApplicationShuttingDown = true;
      LOG.info("Graceful shutdown detected, stopping event processors...");
    }));
  }

  @Override
  public boolean isGracefulShutdown() {
    return isApplicationShuttingDown;
  }
}
