package se.aourell.exampleserver;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.aourell.exampleserver.patientfeed.AssessmentEnded;
import se.aourell.exampleserver.patientfeed.AssessmentStarted;
import se.aourell.exampleserver.patientfeed.PatientAdded;
import se.aourell.exampleserver.patientfeed.PatientDeleted;
import se.aourell.exampleserver.patientfeed.PatientEvent;
import se.aourell.httpfeeds.server.spi.EventBus;

import java.time.Instant;
import java.util.Random;

@Component
public class RandomDataGenerator {

  private final EventBus<PatientEvent> eventBus;

  public RandomDataGenerator(EventBus<PatientEvent> eventBus) {
    this.eventBus = eventBus;
  }

  @Scheduled(initialDelayString = "PT1S", fixedDelayString = "PT0.200S")
  void generateEventData() throws InterruptedException {
    final var eventId = randomId();
    final var event = randomEvent(eventId);
    eventBus.publish(eventId, event);
  }

  private String randomId() {
    return Integer.toString(new Random().nextInt(10_000_000));
  }

  private PatientEvent randomEvent(String id) {
    return switch (new Random().nextInt(0, 4)) {
      case 0 -> new PatientAdded(id, "Scooby", "Doe");
      case 1 -> new AssessmentStarted(id, "A001", Instant.now(), Instant.now().plusSeconds(60*60*24*7));
      case 2 -> new AssessmentEnded(id);
      case 3 -> new PatientDeleted(id);
      default -> null;
    };
  }
}
