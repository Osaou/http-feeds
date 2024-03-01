package se.aourell.httpfeeds.producer.api;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static se.aourell.httpfeeds.producer.api.FeedAvailability.PUBLISH_OVER_HTTP;
import static se.aourell.httpfeeds.producer.spi.FeedItemRepository.DEFAULT_TABLE_NAME;

public class EventFeedsProducerApiTest {

  @Test
  public void publishEventFeed_should_use_default_persistenceName() {
    /*
     * GIVEN
     */

    final EventFeedsProducerApi eventFeedsProducerApi = spy(EventFeedsProducerApi.class);
    final String feedName = "test-feed";
    final Class<String> feedEventBaseType = String.class;
    final FeedAvailability availability = PUBLISH_OVER_HTTP;

    /*
     * WHEN
     */

    eventFeedsProducerApi.publishEventFeed(feedName, feedEventBaseType, availability);

    /*
     * THEN
     */

    verify(eventFeedsProducerApi).publishEventFeed(eq(feedName), eq(feedEventBaseType), eq(availability), eq(DEFAULT_TABLE_NAME));
  }
}
