package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import se.aourell.httpfeeds.producer.core.EventFeedsUtil;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ConstructorBinding
@ConfigurationProperties(prefix = "eventfeeds.producer")
public class ProducerProperties {

  private final Duration pollInterval;
  private final Integer limit;
  private final Set<String> httpFeedsToPublish;

  public ProducerProperties(Duration pollInterval, Integer limit, Map<String, String> publish) {
    this.pollInterval = Optional.ofNullable(pollInterval)
      .orElse(EventFeedsUtil.DEFAULT_POLL_INTERVAL);

    this.limit = Optional.ofNullable(limit)
      .orElse(EventFeedsUtil.DEFAULT_LIMIT_COUNT_PER_REQUEST);

    this.httpFeedsToPublish = Optional.ofNullable(publish)
      .orElse(Collections.emptyMap())
      .entrySet()
      .stream()
      .filter(entry -> "true".equalsIgnoreCase(entry.getValue().trim()))
      .map(Map.Entry::getKey)
      .collect(Collectors.toSet());
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

  /**
   * List of httpfeed paths to publish.
   */
  public boolean shouldPublishHttpFeed(String path) {
    return httpFeedsToPublish.contains(path);
  }
}
