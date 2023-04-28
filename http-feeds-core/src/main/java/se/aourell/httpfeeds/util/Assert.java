package se.aourell.httpfeeds.util;

import java.util.function.Supplier;

public final class Assert {

  private static final String DEFAULT_ERROR_MESSAGE = "Assertion failed";

  private Assert() { }

  public static String hasStringValue(String string) {
    if (string == null) {
      throw new NullPointerException(DEFAULT_ERROR_MESSAGE);
    }

    if (string.isBlank()) {
      throw new IllegalStateException(DEFAULT_ERROR_MESSAGE);
    }

    return string;
  }

  public static <T> T notNull(T object) {
    if (object == null) {
      throw new NullPointerException(DEFAULT_ERROR_MESSAGE);
    }

    return object;
  }

  public static void that(Boolean condition) {
    if (!condition) {
      throw new IllegalStateException(DEFAULT_ERROR_MESSAGE);
    }
  }

  public static void that(Boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  public static <E extends RuntimeException> void that(Boolean condition, Supplier<E> errorSupplier) {
    if (!condition) {
      throw errorSupplier.get();
    }
  }
}
