package se.aourell.httpfeeds.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Result<V> {

  public static <V> Result<V> success(V value) {
    return new Success<>(value);
  }

  public static <V> Result<V> failure(String message) {
    return new Failure<>(message);
  }

  public static <V> Result<V> failure(Throwable throwable) {
    return new Failure<>(throwable);
  }

  public static <V> Result<V> failure(RuntimeException exception) {
    return new Failure<>(exception);
  }

  public abstract boolean isSuccess();
  public abstract boolean isFailure();

  public abstract V orElseGet(V defaultValue);
  public abstract V orElseGet(Supplier<V> defaultValue);
  public abstract V orElseThrow();
  public abstract V orElseThrow(String message);
  public abstract V orElseThrow(Supplier<String> messageSupplier);
  public abstract V orElseThrow(Function<RuntimeException, ? extends RuntimeException> exceptionSupplier);
  public abstract V orElseThrow(Class<? extends RuntimeException> clazz);
  public abstract Result<V> filter(Function<V, Boolean> f);
  public abstract <U> Result<U> map(Function<V, U> f);
  public abstract <U> Result<U> flatMap(Function<V, Result<U>> f);
  public abstract <F extends Exception> Result<V> mapFailure(Function<Exception, F> exceptionTransformer);
  public abstract <F extends Exception> Result<V> recoverFrom(Class<F> failureType, Supplier<V> recoveryValue);
  public abstract Result<V> peek(Consumer<V> f);
  public abstract Result<V> peekFailure(Consumer<Exception> f);
  public abstract Result<Void> ifSuccess(Consumer<V> f);
  public abstract Result<Void> ifFailure(Consumer<Exception> f);

  public Result<V> orElse(Supplier<Result<V>> defaultValue) {
    return map(x -> this).orElseGet(defaultValue);
  }
}
