package se.aourell.exampleclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.aourell.exampleclient.healthdatafeed.BloodSugarReadingUploaded;
import se.aourell.exampleclient.healthdatafeed.EkgStreamUploaded;
import se.aourell.httpfeeds.consumer.api.EventFeedConsumer;
import se.aourell.httpfeeds.consumer.api.EventHandler;

@Service
@EventFeedConsumer("health-data")
public class HealthDataFeedConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(HealthDataFeedConsumer.class);

  @EventHandler
  public void on(EkgStreamUploaded event) {
    LOG.info("health data event received: {}", event);
  }

  @EventHandler
  public void on(BloodSugarReadingUploaded event) {
    LOG.info("health data event received: {}", event);
  }
}
