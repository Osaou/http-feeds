package se.aourell.exampleclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.aourell.exampleclient.patientfeed.AssessmentEnded;
import se.aourell.exampleclient.patientfeed.AssessmentStarted;
import se.aourell.exampleclient.patientfeed.PatientAdded;
import se.aourell.exampleclient.patientfeed.PatientDeleted;
import se.aourell.httpfeeds.consumer.api.EventHandler;
import se.aourell.httpfeeds.consumer.api.EventFeedConsumer;
import se.aourell.httpfeeds.consumer.core.EventMetaData;

@Service
@EventFeedConsumer("patient")
public class PatientFeedConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(PatientFeedConsumer.class);

  @EventHandler
  public void on(PatientAdded event) {
    LOG.info("remote event received: {}", event);
  }

  @EventHandler
  public void on(PatientDeleted event) {
    LOG.info("remote event received: {}", event);
  }

  @EventHandler
  public void on(AssessmentStarted event) {
    LOG.info("remote event received: {}", event);
  }

  @EventHandler
  public void on(AssessmentEnded event, EventMetaData meta) {
    LOG.info("remote event received: {} with metadata: {}", event, meta);
  }
}
