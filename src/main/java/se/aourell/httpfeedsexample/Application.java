package se.aourell.httpfeedsexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"se.aourell.httpfeeds", "se.aourell.httpfeedsexample"})
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
