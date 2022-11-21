package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import se.aourell.httpfeeds.infrastructure.consumer.jdbc.FeedConsumerRepositoryJdbcImpl;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@ConstructorBinding
@ConfigurationProperties(prefix = "eventfeeds.consumer")
public class ConsumerProperties {

  private final boolean enabled;
  private final String tableName;
  private final Map<String, String> sources;

  public ConsumerProperties(boolean enabled, String tableName, Map<String, String> sources) {
    this.enabled = enabled;
    this.tableName = Optional.ofNullable(tableName).orElse(FeedConsumerRepositoryJdbcImpl.DEFAULT_TABLE_NAME);
    this.sources = Optional.ofNullable(sources).orElse(Collections.emptyMap());
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
   * Mapping of httpfeed paths to remote URLs.
   */
  public Map<String, String> getSources() {
    return sources;
  }
}
