package net.kundzi.sandbox.guice.app;

import com.google.inject.AbstractModule;

public class BillingModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(CreditCardProcessor.class).to(PaypalCreditCardProcessor.class);
    bind(TransactionLog.class).to(DatabaseTransactionLog.class);
  }
}
