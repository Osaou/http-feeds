package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import se.aourell.httpfeeds.infrastructure.consumer.jpa.FeedConsumerEntity;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@ConstructorBinding
@ConfigurationProperties(prefix = "eventfeeds.consumer")
public class ConsumerProperties {

  private final String tableName;
  private final Map<String, String> sources;

  public ConsumerProperties(String tableName, Map<String, String> sources) {
    this.tableName = Optional.ofNullable(tableName)
      .orElse(FeedConsumerEntity.TABLE_NAME);

    this.sources = Optional.ofNullable(sources)
      .orElse(Collections.emptyMap());
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
