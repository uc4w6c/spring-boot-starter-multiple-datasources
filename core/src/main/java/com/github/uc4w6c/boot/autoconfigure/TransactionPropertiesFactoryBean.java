package com.github.uc4w6c.boot.autoconfigure;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.autoconfigure.transaction.TransactionProperties;

public class TransactionPropertiesFactoryBean implements FactoryBean<TransactionProperties> {
  private TransactionProperties transactionProperties;

  public TransactionPropertiesFactoryBean(TransactionProperties transactionProperties) {
    this.transactionProperties = transactionProperties;
  }

  @Override
  public TransactionProperties getObject() throws Exception {
    return transactionProperties;
  }

  @Override
  public Class<?> getObjectType() {
    return TransactionProperties.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
