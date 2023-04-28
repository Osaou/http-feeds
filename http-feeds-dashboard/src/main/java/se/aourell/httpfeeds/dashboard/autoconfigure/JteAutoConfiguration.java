package se.aourell.httpfeeds.dashboard.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.aourell.httpfeeds.dashboard.http.HttpFeedsDashboardController;
import se.aourell.httpfeeds.dashboard.jte.JteRenderer;
import se.aourell.httpfeeds.producer.spi.DomainEventSerializer;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JteAutoConfiguration {

  @Bean
  public HttpFeedsDashboardController httpFeedsDashboardController(DeadLetterQueueRepository deadLetterQueueRepository,
                                                                   JteRenderer jteRenderer,
                                                                   @Qualifier("jsonValidator") ObjectMapper jsonValidator,
                                                                   DomainEventSerializer domainEventSerializer) {
    return new HttpFeedsDashboardController(deadLetterQueueRepository, jteRenderer, jsonValidator, domainEventSerializer);
  }

  @Bean
  public JteRenderer jteRenderer() {
    return new JteRenderer();
  }
}
