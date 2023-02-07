package se.aourell.exampleserver;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.aourell.exampleserver.healthdatafeed.BloodSugarReadingUploaded;
import se.aourell.exampleserver.healthdatafeed.EkgStreamUploaded;
import se.aourell.exampleserver.healthdatafeed.HealthDataEvent;
import se.aourell.exampleserver.patientfeed.AssessmentEnded;
import se.aourell.exampleserver.patientfeed.AssessmentStarted;
import se.aourell.exampleserver.patientfeed.PatientAdded;
import se.aourell.exampleserver.patientfeed.PatientDeleted;
import se.aourell.exampleserver.patientfeed.PatientEvent;
import se.aourell.httpfeeds.producer.api.EventFeedCreator;
import se.aourell.httpfeeds.producer.api.FeedAvailability;
import se.aourell.httpfeeds.producer.spi.EventBus;

import java.time.Instant;
import java.util.Random;

@Component
public class RandomDataGenerator {

  private final EventBus<HealthDataEvent> healthDataEventBus;
  private final EventBus<PatientEvent> patientEventBus;

  public RandomDataGenerator(EventFeedCreator eventFeedCreator, EventBus<PatientEvent> patientEventBus) {
    this.healthDataEventBus = eventFeedCreator.createEventFeed("health-data", HealthDataEvent.class, FeedAvailability.PUBLISH_OVER_HTTP);
    this.patientEventBus = patientEventBus;
  }

  @Scheduled(initialDelayString = "PT1S", fixedDelayString = "PT0.200S")
  void generateEventData() {
    final var eventId = randomId();
    final var event = randomEvent(eventId);

    if (event instanceof PatientEvent patientEvent) {
      patientEventBus.publish(eventId, patientEvent);
    }
    else if (event instanceof HealthDataEvent healthDataEvent) {
      healthDataEventBus.publish(eventId, healthDataEvent);
    }
  }

  private String randomId() {
    return Integer.toString(new Random().nextInt(10_000_000));
  }

  private Object randomEvent(String id) {
    return switch (new Random().nextInt(0, 6)) {
      case 0 -> new PatientAdded(id, "Scooby", "Doe");
      case 1 -> new AssessmentStarted(id, "a001", Instant.now(), Instant.now().plusSeconds(60*60*24*7));
      case 2 -> new AssessmentEnded(id);
      case 3 -> new PatientDeleted(id);
      case 4 -> new EkgStreamUploaded(id, "b001", 500, new byte[]{ 1,2,3,4,5 });
      case 5 -> new BloodSugarReadingUploaded(id, "c001", new Random().nextInt(1_000));
      default -> null;
    };
  }
}
