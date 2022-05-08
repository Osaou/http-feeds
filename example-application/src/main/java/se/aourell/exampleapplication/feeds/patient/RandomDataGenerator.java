package se.aourell.exampleapplication.feeds.patient;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.aourell.httpfeeds.spi.EventBus;

import java.time.OffsetDateTime;
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

    Thread.sleep(200L);
  }

  private String randomId() {
    return Integer.toString(new Random().nextInt(10_000_000));
  }

  private PatientEvent randomEvent(String id) {
    return switch (new Random().nextInt(0, 4)) {
      case 0 -> new PatientAdded(id, "Scooby", "Doe");
      case 1 -> new AssessmentStarted(id, "A001", OffsetDateTime.MIN, OffsetDateTime.MAX);
      case 2 -> new AssessmentEnded(id);
      case 3 -> new PatientDeleted(id);
      default -> null;
    };
  }
}
