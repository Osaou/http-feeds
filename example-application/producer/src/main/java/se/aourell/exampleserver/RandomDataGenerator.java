package se.aourell.exampleserver;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.aourell.exampleserver.healthdatafeed.BloodSugarReadingUploaded;
import se.aourell.exampleserver.healthdatafeed.EkgStreamAnalyzed;
import se.aourell.exampleserver.healthdatafeed.EkgStreamUploaded;
import se.aourell.exampleserver.healthdatafeed.HealthDataEvent;
import se.aourell.exampleserver.patientfeed.AssessmentEnded;
import se.aourell.exampleserver.patientfeed.AssessmentStarted;
import se.aourell.exampleserver.patientfeed.PatientAdded;
import se.aourell.exampleserver.patientfeed.PatientDeleted;
import se.aourell.exampleserver.patientfeed.PatientEvent;
import se.aourell.httpfeeds.producer.api.EventFeedsProducerApi;
import se.aourell.httpfeeds.producer.api.FeedAvailability;
import se.aourell.httpfeeds.producer.spi.EventBus;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Component
public class RandomDataGenerator {

  private final EventBus<HealthDataEvent> healthDataEventBus;
  private final EventBus<PatientEvent> patientEventBus;

  public RandomDataGenerator(EventFeedsProducerApi eventFeedsProducerApi, EventBus<PatientEvent> patientEventBus) {
    this.healthDataEventBus = eventFeedsProducerApi.publishEventFeed("health-data", HealthDataEvent.class, FeedAvailability.PUBLISH_OVER_HTTP);
    this.patientEventBus = patientEventBus;
  }

  @Scheduled(initialDelayString = "PT1S", fixedDelayString = "PT0.200S")
  void generateEventData() {
    final var eventId = randomId();
    final var event = randomEvent(eventId);

    if (event instanceof PatientEvent patientEvent) {
      patientEventBus.publish(eventId, patientEvent);
    } else if (event instanceof HealthDataEvent healthDataEvent) {
      if (healthDataEvent instanceof EkgStreamUploaded) {
        final var traceId = UUID.randomUUID().toString();
        healthDataEventBus.publish(eventId, traceId, healthDataEvent);

        final var eventId2 = randomId();
        final var event2 = new EkgStreamAnalyzed(eventId2, 500, "SVES:" + randomInt(10) + ", VES:" + randomInt(10));
        healthDataEventBus.publish(eventId2, traceId, event2);
      } else {
        healthDataEventBus.publish(eventId, healthDataEvent);
      }
    }
  }

  private Object randomEvent(String id) {
    return switch (randomInt(6)) {
      case 0 -> new PatientAdded(id, "Scooby", "Doe");
      case 1 -> new AssessmentStarted(id, "a001", Instant.now(), Instant.now().plusSeconds(60*60*24*7));
      case 2 -> new AssessmentEnded(id);
      case 3 -> new PatientDeleted(id);
      case 4 -> new EkgStreamUploaded(id, "b001", "https://object-storage.domain.com/b001");
      case 5 -> new BloodSugarReadingUploaded(id, "c001", randomInt(1_000));
      default -> null;
    };
  }

  private long id = 0;

  private String randomId() {
    return Long.toString(++id);
  }

  private int randomInt(int upperBound) {
    return new Random().nextInt(upperBound);
  }
}
