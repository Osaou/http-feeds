package se.aourell.exampleserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.aourell.exampleserver.patientfeed.AssessmentEnded;
import se.aourell.exampleserver.patientfeed.AssessmentStarted;
import se.aourell.exampleserver.patientfeed.PatientAdded;
import se.aourell.exampleserver.patientfeed.PatientDeleted;
import se.aourell.httpfeeds.consumer.api.EventFeedConsumer;
import se.aourell.httpfeeds.consumer.api.EventHandler;
import se.aourell.httpfeeds.consumer.core.EventMetaData;

@Service
@EventFeedConsumer("patient")
public class LocalPatientFeedConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(LocalPatientFeedConsumer.class);

  @EventHandler
  public void on(PatientAdded event) {
    LOG.info("local event received: {}", event);
  }

  @EventHandler
  public void on(PatientDeleted event) {
    LOG.info("local event received: {}", event);
  }

  @EventHandler
  public void on(AssessmentStarted event) {
    LOG.info("local event received: {}", event);
  }

  @EventHandler
  public void on(AssessmentEnded event, EventMetaData meta) {
    LOG.info("local event received: {} with metadata: {}", event, meta);
  }
}
