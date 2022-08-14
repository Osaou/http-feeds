package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import se.aourell.httpfeeds.server.spi.FeedItemService;

import java.time.Duration;
import java.util.Optional;

@ConstructorBinding
@ConfigurationProperties(prefix = "httpfeeds.server")
public class ServerProperties {

  private final boolean enabled;
  private final Duration pollInterval;
  private final Integer limit;

  public ServerProperties(boolean enabled, Duration pollInterval, Integer limit) {
    this.enabled = enabled;
    this.pollInterval = Optional.ofNullable(pollInterval).orElse(FeedItemService.DEFAULT_POLL_INTERVAL);
    this.limit = Optional.ofNullable(limit).orElse(FeedItemService.DEFAULT_LIMIT_COUNT_PER_REQUEST);
  }

  /**
   * Whether to enable the server part or not. Default false.
   */
  public boolean isEnabled() {
    return enabled;
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
