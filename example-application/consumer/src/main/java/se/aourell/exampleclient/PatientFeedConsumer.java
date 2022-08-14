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

@Service
@HttpFeedConsumer(feedName = "patient")
public class PatientFeedConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(PatientFeedConsumer.class);

  private void logEvent(Object event) {
    LOG.info("event received: {}", event);
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
