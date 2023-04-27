package se.aourell.httpfeeds.producer.core;

import java.time.Duration;

public abstract class EventFeedsUtil {

  public static final String PATH_PREFIX = "/feed";
  public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(1);
  public static final int DEFAULT_LIMIT_COUNT_PER_REQUEST = 1_000;

  public static String validateFeedName(String name) {
    if (name == null || "".equalsIgnoreCase(name.trim())) {
      throw new IllegalArgumentException("Feed name must not be empty");
    }

    return name;
  }

  public static String fullUrlFromBaseUriAndFeedName(String baseUri, String name) {
    final String feedPath = baseUri.endsWith("/")
      ? urlPathFromFeedName(name).substring(1)
      : urlPathFromFeedName(name);
    return baseUri + feedPath;
  }

  public static String urlPathFromFeedName(String name) {
    return EventFeedsUtil.PATH_PREFIX + "/" + name;
  }
}
