package se.aourell.httpfeeds.producer.spi;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class EventBusTest {

  /**
   * Room for improvment...
   */
  @Test
  void eventBus_should_use_now_as_default_time() {
    /*
     * GIVEN
     */

    final String subject = "subject";
    final EventBus<Integer> eventBus = spy(EventBus.class);
    final int event = 42;

    /*
     * WHEN
     */

    // NOTE: do we need to care about CPU reordering here?
    final Instant before = Instant.now();
    eventBus.publish(subject, event);
    final Instant after = Instant.now();

    /*
     * THEN
     */

    final ArgumentCaptor<Instant> timeCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(eventBus).publish(eq(subject), eq(subject), eq(event), timeCaptor.capture());

    final Instant time = timeCaptor.getValue();
    assertTrue(time.isAfter(before) || time.equals(before));
    assertTrue(time.isBefore(after) || time.equals(after));
  }

  /**
   * Room for improvment...
   */
  @Test
  void eventBus_should_use_now_as_default_time_when_trace_is_specified() {
    /*
     * GIVEN
     */

    final String subject = "subject";
    final String trace = "trace id";
    final EventBus<Integer> eventBus = spy(EventBus.class);
    final int event = 42;

    /*
     * WHEN
     */

    // NOTE: do we need to care about CPU reordering here?
    final Instant before = Instant.now();
    eventBus.publish(subject, trace, event);
    final Instant after = Instant.now();

    /*
     * THEN
     */

    final ArgumentCaptor<Instant> timeCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(eventBus).publish(eq(subject), eq(trace), eq(event), timeCaptor.capture());

    final Instant time = timeCaptor.getValue();
    assertTrue(time.isAfter(before) || time.equals(before));
    assertTrue(time.isBefore(after) || time.equals(after));
  }

  @Test
  void eventBus_should_use_subject_as_default_trace_id() {
    /*
     * GIVEN
     */

    final String subject = "subject";
    final EventBus<Integer> eventBus = spy(EventBus.class);
    final int event = 42;

    /*
     * WHEN
     */

    eventBus.publish(subject, event);

    /*
     * THEN
     */

    verify(eventBus).publish(eq(subject), eq(subject), eq(event), any());
  }

  @Test
  void eventBus_should_use_subject_as_default_trace_id_when_time_is_specified() {
    /*
     * GIVEN
     */

    final String subject = "subject";
    final Instant time = Instant.MIN;
    final EventBus<Integer> eventBus = spy(EventBus.class);
    final int event = 42;

    /*
     * WHEN
     */

    eventBus.publish(subject, event, time);

    /*
     * THEN
     */

    verify(eventBus).publish(subject, subject, event, time);
  }
}
