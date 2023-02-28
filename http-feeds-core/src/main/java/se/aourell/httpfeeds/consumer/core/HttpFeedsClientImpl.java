package se.aourell.httpfeeds.consumer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.consumer.spi.CloudEventArrayDeserializer;
import se.aourell.httpfeeds.consumer.spi.HttpFeedsClient;
import se.aourell.httpfeeds.CloudEvent;
import se.aourell.httpfeeds.util.Result;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HttpFeedsClientImpl implements HttpFeedsClient {

  private static final Logger LOG = LoggerFactory.getLogger(HttpFeedsClientImpl.class);
  private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(3);

  private final CloudEventArrayDeserializer cloudEventArrayDeserializer;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  public HttpFeedsClientImpl(CloudEventArrayDeserializer cloudEventArrayDeserializer) {
    this.cloudEventArrayDeserializer = cloudEventArrayDeserializer;
  }

  @Override
  public Result<List<CloudEvent>> pollCloudEvents(String httpFeedUrl, String lastProcessedId) {
    final var urlWithLastProcessedId = Optional.ofNullable(lastProcessedId)
      .map(id -> httpFeedUrl + "?lastEventId=" + id)
      .orElse(httpFeedUrl);

    final URI uri;
    try {
      uri = new URI(urlWithLastProcessedId);
    } catch (URISyntaxException e) {
      LOG.error("Exception when parsing httpfeed url " + urlWithLastProcessedId, e);
      return Result.failure(e);
    }

    final var request = HttpRequest.newBuilder()
      .uri(uri)
      .timeout(HTTP_TIMEOUT)
      .GET()
      .build();

    final HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (HttpTimeoutException e) {
      return Result.success(Collections.emptyList());
    } catch (IOException | InterruptedException e) {
      final var err = new RuntimeException("Exception when fetching cloud events from url" + urlWithLastProcessedId, e);
      return Result.failure(err);
    }

    if (response.statusCode() != 200) {
      return Result.failure("Unexpected status code " + response.statusCode() + " when fetching cloud events from url " + urlWithLastProcessedId);
    }

    final var body = response.body();
    final var events = Arrays.stream(cloudEventArrayDeserializer.toCloudEvents(body))
      .toList();

    return Result.success(events);
  }
}
