package com.github.uc4w6c.boot.autoconfigure;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

public class DataSourcePropertiesFactoryBean implements FactoryBean<DataSourceProperties> {
  private DataSourceProperties dataSourceProperties;

  public DataSourcePropertiesFactoryBean(DataSourceProperties dataSourceProperties) {
    this.dataSourceProperties = dataSourceProperties;
  }

  @Override
  public DataSourceProperties getObject() throws Exception {
    return dataSourceProperties;
  }

  @Override
  public Class<?> getObjectType() {
    return DataSourceProperties.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
