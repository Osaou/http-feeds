package se.aourell.httpfeeds.dashboard.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.aourell.httpfeeds.dashboard.http.HttpFeedsDashboardController;
import se.aourell.httpfeeds.dashboard.jte.JteRenderer;
import se.aourell.httpfeeds.tracing.spi.DeadLetterQueueRepository;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JteAutoConfiguration {

  @Bean
  public HttpFeedsDashboardController httpFeedsDashboardController(DeadLetterQueueRepository deadLetterQueueRepository, JteRenderer jteRenderer) {
    return new HttpFeedsDashboardController(deadLetterQueueRepository, jteRenderer);
  }

  @Bean
  public JteRenderer jteRenderer() {
    return new JteRenderer();
  }
}
