package se.aourell.httpfeeds.infrastructure.spring;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import se.aourell.httpfeeds.TransactionContext;
import se.aourell.httpfeeds.util.Assert;

public class TransactionContextImpl implements TransactionContext {

  private final PlatformTransactionManager platformTransactionManager;
  private final DefaultTransactionDefinition transactionDefinition;

  public TransactionContextImpl(PlatformTransactionManager platformTransactionManager) {
    this.platformTransactionManager = Assert.notNull(platformTransactionManager);

    transactionDefinition = new DefaultTransactionDefinition();
    transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  @Override
  public void executeInNewTransaction(Runnable runnable) {
    final var tx = platformTransactionManager.getTransaction(transactionDefinition);
    try {
      runnable.run();
      platformTransactionManager.commit(tx);
    } catch (Throwable e) {
      platformTransactionManager.rollback(tx);
      throw e;
    }
  }
}
