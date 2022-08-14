package se.aourell.httpfeeds.core.util;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Result<V> {

  public static <V> Result<V> success(V value) {
    return new Success<>(value);
  }

  public static <V> Result<V> failure(String message) {
    return new Failure<>(message);
  }

  public static <V> Result<V> failure(Exception exception) {
    return new Failure<>(exception);
  }

  public static <V> Result<V> failure(RuntimeException exception) {
    return new Failure<>(exception);
  }

  public abstract V orElseGet(final V defaultValue);
  public abstract V orElseGet(final Supplier<V> defaultValue);
  public abstract <U> Result<U> map(Function<V, U> f);
  public abstract <U> Result<U> flatMap(Function<V, Result<U>> f);

  public Result<V> orElse(Supplier<Result<V>> defaultValue) {
    return map(x -> this).orElseGet(defaultValue);
  }



  private static final class Success<V> extends Result<V> {
    private final V value;

    private Success(V value) {
      this.value = value;
    }

    public V orElseGet(V defaultValue) {
      return value;
    }

    public V orElseGet(Supplier<V> defaultValue) {
      return value;
    }

    public <U> Result<U> map(Function<V, U> f) {
      try {
        return success(f.apply(value));
      } catch (Exception e) {
        return failure(e);
      }
    }

    public <U> Result<U> flatMap(Function<V, Result<U>> f) {
      try {
        return f.apply(value);
      } catch (Exception e) {
        return failure(e);
      }
    }
  }



  private static final class Failure<V> extends Result<V> {
    private final RuntimeException exception;

    private Failure(String message) {
      this.exception = new IllegalStateException(message);
    }

    private Failure(Exception exception) {
      this.exception = new IllegalStateException(exception);
    }

    private Failure(RuntimeException exception) {
      this.exception = exception;
    }

    public V orElseGet(V defaultValue) {
      return defaultValue;
    }

    public V orElseGet(Supplier<V> defaultValue) {
      return defaultValue.get();
    }

    public <U> Result<U> map(Function<V, U> f) {
      return failure(exception);
    }

    public <U> Result<U> flatMap(Function<V, Result<U>> f) {
      return failure(exception);
    }
  }
}
