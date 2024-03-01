package se.aourell.httpfeeds.consumer.spi;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class HttpFeedsClientTest {

  @Test
  void pollCloudEvents_should_use_null_as_default_lastProcessedId() {
    /*
     * GIVEN
     */

    final HttpFeedsClient httpFeedsClient = spy(HttpFeedsClient.class);
    final String httpFeedUrl = "http://test.test";

    /*
     * WHEN
     */

    httpFeedsClient.pollCloudEvents(httpFeedUrl);

    /*
     * THEN
     */

    verify(httpFeedsClient).pollCloudEvents(eq(httpFeedUrl), isNull());
  }
}
