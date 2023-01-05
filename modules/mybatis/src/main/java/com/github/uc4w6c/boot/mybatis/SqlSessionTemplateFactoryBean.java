package com.github.uc4w6c.boot.mybatis;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;

public class SqlSessionTemplateFactoryBean implements FactoryBean<SqlSessionTemplate> {
  private SqlSessionTemplate sqlSessionTemplate;

  public SqlSessionTemplateFactoryBean(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  @Override
  public SqlSessionTemplate getObject() throws Exception {
    return sqlSessionTemplate;
  }

  @Override
  public Class<?> getObjectType() {
    return SqlSessionTemplate.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
