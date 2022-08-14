package se.aourell.exampleclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.aourell.exampleapplication.patientfeed.AssessmentEnded;
import se.aourell.exampleapplication.patientfeed.AssessmentStarted;
import se.aourell.exampleapplication.patientfeed.PatientAdded;
import se.aourell.exampleapplication.patientfeed.PatientDeleted;
import se.aourell.httpfeeds.client.api.EventHandler;
import se.aourell.httpfeeds.client.api.HttpFeedConsumer;
import se.aourell.httpfeeds.client.core.EventMetaData;

@Service
@HttpFeedConsumer(feedName = "patient")
public class PatientFeedConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(PatientFeedConsumer.class);

  private void logEvent(Object event) {
    final var meta = EventMetaData.current();
    LOG.info("event received: {}, with metadata: {}", event, meta);
  }

  @EventHandler
  public void on(PatientAdded event) {
    logEvent(event);
  }

  @EventHandler
  public void on(PatientDeleted event) {
    logEvent(event);
  }

  @EventHandler
  public void on(AssessmentStarted event) {
    logEvent(event);
  }

  @EventHandler
  public void on(AssessmentEnded event) {
    logEvent(event);
  }
}
