package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ConstructorBinding
@ConfigurationProperties(prefix = "httpfeeds.client")
public class ClientProperties {

  private final boolean enabled;
  private final String tableName;
  private final Duration pollInterval;
  private final Map<String, String> urls;

  public ClientProperties(boolean enabled, String tableName, Duration pollInterval, Map<String, String> urls) {
    this.enabled = enabled;
    this.tableName = Optional.ofNullable(tableName).orElse("httpfeeds_consummation");
    this.pollInterval = Optional.ofNullable(pollInterval).orElse(Duration.ofSeconds(1));
    this.urls = Optional.ofNullable(urls).orElse(new HashMap<>());
  }

  /**
   * Whether to enable the client part or not. Default false.
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Name of database table to store last processed ID for httpfeed consumers.
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * The polling interval used for fetching new cloud events.
   */
  public Duration getPollInterval() {
    return pollInterval;
  }

  /**
   * Mapping of httpfeed names to URLs.
   */
  public Map<String, String> getUrls() {
    return urls;
  }
}
