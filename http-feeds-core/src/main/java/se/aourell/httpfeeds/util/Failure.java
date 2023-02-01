package se.aourell.httpfeeds.util;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class Failure<V> extends Result<V> {
  private final RuntimeException exception;

  Failure(String message) {
    this.exception = new IllegalStateException(message);
  }

  Failure(Exception exception) {
    this.exception = new IllegalStateException(exception);
  }

  Failure(RuntimeException exception) {
    this.exception = exception;
  }

  @Override
  public boolean isSuccess() {
    return false;
  }

  @Override
  public boolean isFailure() {
    return true;
  }

  @Override
  public V orElseGet(V defaultValue) {
    return defaultValue;
  }

  @Override
  public V orElseGet(Supplier<V> defaultValue) {
    return defaultValue.get();
  }

  @Override
  public V orElseThrow() {
    throw exception;
  }

  @Override
  public V orElseThrow(String message) {
    throw new RuntimeException(message, exception);
  }

  @Override
  public V orElseThrow(Supplier<String> messageSupplier) {
    return orElseThrow(messageSupplier.get());
  }

  @Override
  public V orElseThrow(Function<RuntimeException, ? extends RuntimeException> exceptionSupplier) {
    throw exceptionSupplier.apply(exception);
  }

  @Override
  public V orElseThrow(Class<? extends RuntimeException> clazz) {
    try {
      throw clazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Result<V> filter(Function<V, Boolean> f) {
    return this;
  }

  @Override
  public <U> Result<U> map(Function<V, U> f) {
    return failure(exception);
  }

  @Override
  public <U> Result<U> flatMap(Function<V, Result<U>> f) {
    return failure(exception);
  }

  @Override
  public <F extends Exception> Result<V> mapFailure(Function<Exception, F> exceptionTransformer) {
    try {
      final F transformed = exceptionTransformer.apply(exception);
      return failure(transformed);
    } catch (Exception e) {
      return failure(e);
    }
  }

  @Override
  public <F extends Exception> Result<V> recoverFrom(Class<F> failureType, Supplier<V> recoveryValue) {
    try {
      return exception.getClass().isAssignableFrom(failureType)
        ? success(recoveryValue.get())
        : this;
    } catch (Exception e) {
      return failure(e);
    }
  }

  @Override
  public Result<V> peek(Consumer<V> f) {
    return this;
  }

  @Override
  public Result<V> peekFailure(Consumer<Exception> f) {
    try {
      f.accept(exception);
      return this;
    } catch (Exception e) {
      return failure(e);
    }
  }

  @Override
  public Result<Void> ifSuccess(Consumer<V> f) {
    return failure(exception);
  }

  @Override
  public Result<Void> ifFailure(Consumer<Exception> f) {
    try {
      f.accept(exception);
      return failure(exception);
    } catch (Exception e) {
      return failure(e);
    }
  }
}
