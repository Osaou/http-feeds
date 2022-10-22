package se.aourell.httpfeeds.infrastructure.consumer;

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
  private static final Duration LONG_POLLING_TIMEOUT = Duration.ofSeconds(60);

  private final CloudEventArrayDeserializer cloudEventArrayDeserializer;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  public HttpFeedsClientImpl(CloudEventArrayDeserializer cloudEventArrayDeserializer) {
    this.cloudEventArrayDeserializer = cloudEventArrayDeserializer;
  }

  @Override
  public Result<List<CloudEvent>> pollCloudEvents(String httpFeedUrl, String lastProcessedId) {
    final var urlWithLongPolling = httpFeedUrl + "?timeout=" + LONG_POLLING_TIMEOUT.toMillis();
    final var urlWithLastProcessedId = Optional.ofNullable(lastProcessedId)
      .map(id -> urlWithLongPolling + "&lastEventId=" + id)
      .orElse(urlWithLongPolling);

    final URI uri;
    try {
      uri = new URI(urlWithLastProcessedId);
    } catch (URISyntaxException e) {
      LOG.error("Exception when parsing httpfeed url", e);
      return Result.failure(e);
    }

    final var request = HttpRequest.newBuilder()
      .uri(uri)
      .timeout(LONG_POLLING_TIMEOUT)
      .GET()
      .build();

    final HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (HttpTimeoutException e) {
      return Result.success(Collections.emptyList());
    } catch (IOException | InterruptedException e) {
      LOG.error("Exception when GETing httpfeed from " + httpFeedUrl, e);
      return Result.failure(e);
    }

    final var body = response.body();
    final var events = Arrays.stream(cloudEventArrayDeserializer.toCloudEvents(body))
      .toList();

    return Result.success(events);
  }
}
