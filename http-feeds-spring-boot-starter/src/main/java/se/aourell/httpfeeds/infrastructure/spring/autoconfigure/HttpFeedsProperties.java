package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import se.aourell.httpfeeds.spi.FeedItemService;

import java.time.Duration;
import java.util.Optional;

@ConstructorBinding
@ConfigurationProperties(prefix = "httpfeeds.server")
public class HttpFeedsProperties {

  private final Duration pollInterval;
  private final Integer limit;

  public HttpFeedsProperties(Duration pollInterval, Integer limit) {
    this.pollInterval = Optional.ofNullable(pollInterval).orElse(FeedItemService.DEFAULT_POLL_INTERVAL);
    this.limit = Optional.ofNullable(limit).orElse(FeedItemService.DEFAULT_LIMIT_COUNT_PER_REQUEST);
  }

  /**
   * The polling interval used when fetching with timeout request parameter.
   */
  public Duration getPollInterval() {
    return pollInterval;
  }

  /**
   * The maximum number of events returned per request.
   */
  public Integer getLimit() {
    return limit;
  }
}
