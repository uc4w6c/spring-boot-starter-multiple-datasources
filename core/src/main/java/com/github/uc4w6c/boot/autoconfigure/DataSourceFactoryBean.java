package com.github.uc4w6c.boot.autoconfigure;

import javax.sql.DataSource;
import org.springframework.beans.factory.FactoryBean;

public class DataSourceFactoryBean implements FactoryBean<DataSource> {
  private DataSource dataSource;

  public DataSourceFactoryBean(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public DataSource getObject() throws Exception {
    return dataSource;
  }

  @Override
  public Class<?> getObjectType() {
    return DataSource.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
