package se.aourell.httpfeeds.example;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class HttpFeedRegistryImpl implements HttpFeedRegistry {

  private final Map<String, HttpFeedDefinition> lut = new HashMap<>();

  @Override
  public void defineFeed(HttpFeed feed, FeedFetcher feedFetcher) {
    final var def = new HttpFeedDefinition(feed.feed(), feed.path(), feed.table(), feedFetcher);

    var path = feed.feed();
    while (path.startsWith("/")) {
      path = path.substring(1);
    }
    while (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    lut.put(feed.feed(), def);
  }

  @Override
  public Optional<HttpFeedDefinition> getDefinedFeed(String name) {
    final var def = lut.get(name);
    if (def == null) {
      return Optional.empty();
    }
    return Optional.of(def);
  }
}
