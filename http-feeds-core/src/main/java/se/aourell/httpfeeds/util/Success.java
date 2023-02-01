package se.aourell.httpfeeds.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class Success<V> extends Result<V> {
  private final V value;

  Success(V value) {
    this.value = value;
  }

  @Override
  public boolean isSuccess() {
    return true;
  }

  @Override
  public boolean isFailure() {
    return false;
  }

  @Override
  public V orElseGet(V defaultValue) {
    return value;
  }

  @Override
  public V orElseGet(Supplier<V> defaultValue) {
    return value;
  }

  @Override
  public V orElseThrow() {
    return value;
  }

  @Override
  public V orElseThrow(String message) {
    return value;
  }

  @Override
  public V orElseThrow(Supplier<String> messageSupplier) {
    return value;
  }

  @Override
  public V orElseThrow(Function<RuntimeException, ? extends RuntimeException> exceptionSupplier) {
    return value;
  }

  @Override
  public V orElseThrow(Class<? extends RuntimeException> clazz) {
    return value;
  }

  @Override
  public Result<V> filter(Function<V, Boolean> f) {
    try {
      return f.apply(value)
        ? this
        : failure(new IllegalStateException("filter() method of Result type failed"));
    } catch (Exception e) {
      return failure(e);
    }
  }

  @Override
  public <U> Result<U> map(Function<V, U> f) {
    try {
      return success(f.apply(value));
    } catch (Exception e) {
      return failure(e);
    }
  }

  @Override
  public <U> Result<U> flatMap(Function<V, Result<U>> f) {
    try {
      return f.apply(value);
    } catch (Exception e) {
      return failure(e);
    }
  }

  @Override
  public <F extends Exception> Result<V> mapFailure(Function<Exception, F> exceptionTransformer) {
    return this;
  }

  @Override
  public <F extends Exception> Result<V> recoverFrom(Class<F> failureType, Supplier<V> recoveryValue) {
    return this;
  }

  @Override
  public Result<V> peek(Consumer<V> f) {
    try {
      f.accept(value);
      return this;
    } catch (Exception e) {
      return failure(e);
    }
  }

  @Override
  public Result<V> peekFailure(Consumer<Exception> f) {
    return this;
  }

  @Override
  public Result<Void> ifSuccess(Consumer<V> f) {
    try {
      f.accept(value);
      return success(null);
    } catch (Exception e) {
      return failure(e);
    }
  }

  @Override
  public Result<Void> ifFailure(Consumer<Exception> f) {
    return success(null);
  }
}
