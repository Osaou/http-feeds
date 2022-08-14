package se.aourell.httpfeeds.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.aourell.httpfeeds.client.spi.CloudEventArrayDeserializer;
import se.aourell.httpfeeds.client.spi.HttpFeedsClient;
import se.aourell.httpfeeds.core.CloudEvent;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
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
  public List<CloudEvent> pollCloudEvents(String url, String lastProcessedId) {
    final var urlWithLongPolling = url + "?timeout=" + LONG_POLLING_TIMEOUT.toMillis();
    final var urlWithLastProcessedId = Optional.ofNullable(lastProcessedId)
      .map(id -> urlWithLongPolling + "&lastEventId=" + id)
      .orElse(urlWithLongPolling);

    final URI uri;
    try {
      uri = new URI(urlWithLastProcessedId);
    } catch (URISyntaxException e) {
      LOG.error("Exception when parsing httpfeed url", e);
      return List.of();
    }

    final HttpRequest request = HttpRequest.newBuilder()
      .uri(uri)
      .timeout(LONG_POLLING_TIMEOUT)
      .GET()
      .build();

    final HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      return List.of();
    }

    return Arrays.stream(cloudEventArrayDeserializer.toCloudEvents(response.body()))
      .toList();
  }
}
