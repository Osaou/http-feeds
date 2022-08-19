package se.aourell.exampleclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.aourell.exampleclient.patientfeed.AssessmentEnded;
import se.aourell.exampleclient.patientfeed.AssessmentStarted;
import se.aourell.exampleclient.patientfeed.PatientAdded;
import se.aourell.exampleclient.patientfeed.PatientDeleted;
import se.aourell.httpfeeds.client.api.EventHandler;
import se.aourell.httpfeeds.client.api.HttpFeedConsumer;
import se.aourell.httpfeeds.client.core.EventMetaData;

@Service
@HttpFeedConsumer(name = "patient")
public class PatientFeedConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(PatientFeedConsumer.class);

  @EventHandler
  public void on(PatientAdded event) {
    LOG.info("event received: {}", event);
  }

  @EventHandler
  public void on(PatientDeleted event) {
    LOG.info("event received: {}", event);
  }

  @EventHandler
  public void on(AssessmentStarted event) {
    LOG.info("event received: {}", event);
  }

  @EventHandler
  public void on(AssessmentEnded event, EventMetaData meta) {
    LOG.info("event received: {} with metadata: {}", event, meta);
  }
}
