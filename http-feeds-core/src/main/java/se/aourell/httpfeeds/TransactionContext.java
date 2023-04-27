package se.aourell.httpfeeds;

public interface TransactionContext {

  void executeInNewTransaction(Runnable runnable);
}
