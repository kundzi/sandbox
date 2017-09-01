package net.kundzi.sandbox.guice.app;

import com.google.inject.AbstractModule;

import javax.inject.Inject;

public class BillingService extends AbstractModule {

  private final CreditCardProcessor processor;
  private final TransactionLog transactionLog;

  @Inject
  public BillingService(final CreditCardProcessor processor,
                        final TransactionLog transactionLog) {
    this.processor = processor;
    this.transactionLog = transactionLog;
  }

  Receipt changeOrder(PizzaOrder order, CreditCard creditCard) {
    return new Receipt();
  }

  @Override
  protected void configure() {
    
  }
}
