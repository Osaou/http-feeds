package se.aourell.httpfeeds.tracing.core;

public class DeadLetterQueueException extends RuntimeException {

  public DeadLetterQueueException(Throwable cause) {
    super(cause);
  }
}
