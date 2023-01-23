package se.aourell.exampleclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.aourell.exampleclient.healthdatafeed.EkgStreamUploaded;
import se.aourell.exampleclient.patientfeed.AssessmentEnded;
import se.aourell.exampleclient.patientfeed.AssessmentStarted;
import se.aourell.httpfeeds.consumer.api.FeedConsumerRegistry;
import se.aourell.httpfeeds.consumer.core.EventMetaData;

@Service
public class ProgrammaticallyRegisteredFeedConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(ProgrammaticallyRegisteredFeedConsumer.class);

  public ProgrammaticallyRegisteredFeedConsumer(FeedConsumerRegistry feedConsumerRegistry) {
    feedConsumerRegistry.registerRemoteConsumer("patient")
      .registerHandler(AssessmentStarted.class, this::onAssessmentStarted)
      .registerHandler(AssessmentEnded.class, this::onAssessmentEnded);

    feedConsumerRegistry.registerRemoteConsumer("health-data")
      .registerHandler(EkgStreamUploaded.class, this::onEkgStreamUploaded);
  }

  private void onAssessmentStarted(AssessmentStarted event) {
    LOG.info("event received: {}", event);
  }

  private void onAssessmentEnded(AssessmentEnded event) {
    LOG.info("event received: {}", event);
  }

  private void onEkgStreamUploaded(EkgStreamUploaded event, EventMetaData meta) {
    LOG.info("event received: {} with metadata: {}", event, meta);
  }
}
