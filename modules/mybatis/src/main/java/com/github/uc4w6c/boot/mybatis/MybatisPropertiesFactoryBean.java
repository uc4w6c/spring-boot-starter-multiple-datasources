package com.github.uc4w6c.boot.mybatis;

import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.factory.FactoryBean;

public class MybatisPropertiesFactoryBean implements FactoryBean<MybatisProperties> {
  private MybatisProperties mybatisProperties;

  public MybatisPropertiesFactoryBean(MybatisProperties mybatisProperties) {
    this.mybatisProperties = mybatisProperties;
  }

  @Override
  public MybatisProperties getObject() throws Exception {
    return mybatisProperties;
  }

  @Override
  public Class<?> getObjectType() {
    return MybatisProperties.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
