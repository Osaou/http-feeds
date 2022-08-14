package se.aourell.httpfeeds.infrastructure.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import se.aourell.httpfeeds.infrastructure.client.FeedConsumerRepositoryImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ConstructorBinding
@ConfigurationProperties(prefix = "httpfeeds.client")
public class ClientProperties {

  private final boolean enabled;
  private final String tableName;
  private final Map<String, String> urls;

  public ClientProperties(boolean enabled, String tableName, Map<String, String> urls) {
    this.enabled = enabled;
    this.tableName = Optional.ofNullable(tableName).orElse(FeedConsumerRepositoryImpl.DEFAULT_TABLE_NAME);
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
   * Mapping of httpfeed names to URLs.
   */
  public Map<String, String> getUrls() {
    return urls;
  }
}
