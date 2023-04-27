package se.aourell.exampleclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.aourell.exampleclient.healthdatafeed.BloodSugarReadingUploaded;
import se.aourell.exampleclient.healthdatafeed.EkgStreamAnalyzed;
import se.aourell.exampleclient.healthdatafeed.EkgStreamUploaded;
import se.aourell.httpfeeds.consumer.api.EventFeedConsumer;
import se.aourell.httpfeeds.consumer.api.EventHandler;

import java.util.Random;

@Service
@EventFeedConsumer("health-data")
public class SingleFeedConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(SingleFeedConsumer.class);

  @EventHandler
  public void on(EkgStreamUploaded event) {
    LOG.info("health data event received: {}", event);

    // simulate failure scenario: 30% chance it will throw an exception
    if (new Random().nextInt(3) == 0) {
      throw new RuntimeException("oops");
    }
  }

  @EventHandler
  public void on(EkgStreamAnalyzed event) {
    LOG.info("health data event received: {}", event);
  }

  @EventHandler
  public void on(BloodSugarReadingUploaded event) {
    LOG.info("health data event received: {}", event);
  }
}
