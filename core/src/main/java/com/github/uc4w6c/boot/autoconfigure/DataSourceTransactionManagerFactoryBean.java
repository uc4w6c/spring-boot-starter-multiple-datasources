package com.github.uc4w6c.boot.autoconfigure;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

public class DataSourceTransactionManagerFactoryBean implements FactoryBean<DataSourceTransactionManager> {
  private DataSourceTransactionManager dataSourceTransactionManager;

  public DataSourceTransactionManagerFactoryBean(DataSourceTransactionManager dataSourceTransactionManager) {
    this.dataSourceTransactionManager = dataSourceTransactionManager;
  }

  @Override
  public DataSourceTransactionManager getObject() throws Exception {
    return dataSourceTransactionManager;
  }

  @Override
  public Class<?> getObjectType() {
    return DataSourceTransactionManager.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
