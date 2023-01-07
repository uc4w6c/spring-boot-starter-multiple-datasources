package com.github.uc4w6c.boot.mybatis;

import com.github.uc4w6c.boot.autoconfigure.MultipleDataSourcesConfiguration;
import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass(DataSource.class)
// @EnableConfigurationProperties(MybatisMultipleDataSourcesProperties.class)
@Import(MybatisMultipleDatasourcesConfiguration.MybatisMultipleDatasourcesRegistar.class)
@AutoConfigureAfter(MultipleDataSourcesConfiguration.class)
public class MybatisMultipleDatasourcesConfiguration {
  static class MybatisMultipleDatasourcesRegistar
      implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {
    private final String MYBATIS_PREFIX = "mybatis";
    private static final String SUFFIXED_MYBATIS_PROPERTIES_BEAN_NAME = "_mybatis_properties";
    private static final String SUFFIXED_SQL_SESSION_FACTORY_PROPERTIES_BEAN_NAME =
        "_sql_session_factory";
    private static final String SUFFIXED_SQL_SESSION_TEMPLATE_PROPERTIES_BEAN_NAME =
        "_sql_session_template";

    private Map<String, MybatisProperties> multipleMybatisProperties;
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
      this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
      BindResult<Map<String, MybatisProperties>> bind =
          Binder.get(environment)
              .bind(MYBATIS_PREFIX, Bindable.mapOf(String.class, MybatisProperties.class));

      multipleMybatisProperties =
          bind.orElseThrow(
              () -> new MybatisMultipleException(String.format("Please set %s", MYBATIS_PREFIX)));
      if (multipleMybatisProperties.isEmpty()) {
        throw new MybatisMultipleException("Please Set at least one Datasource.");
      }
    }

    @Override
    public void registerBeanDefinitions(
        AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
      for (Map.Entry<String, MybatisProperties> entry : multipleMybatisProperties.entrySet()) {

        registerBeanMybatisProperties(entry.getKey(), entry.getValue(), registry);
        SqlSessionFactory sqlSessionFactory =
            registerBeanSqlSessionFactory(entry.getKey(), entry.getValue(), registry);
        registerBeanSqlSessionTemplate(
            entry.getKey(), entry.getValue(), sqlSessionFactory, registry);
      }
    }

    private void registerBeanMybatisProperties(
        String keyName, MybatisProperties mybatisProperties, BeanDefinitionRegistry registry) {
      BeanDefinitionBuilder mybatisPropertiesBuild =
          BeanDefinitionBuilder.genericBeanDefinition(MybatisPropertiesFactoryBean.class);
      GenericBeanDefinition mybatisPropertiesBeanDefinition =
          (GenericBeanDefinition) mybatisPropertiesBuild.getBeanDefinition();
      mybatisPropertiesBeanDefinition
          .getConstructorArgumentValues()
          .addGenericArgumentValue(mybatisProperties);

      registry.registerBeanDefinition(
          keyName + SUFFIXED_MYBATIS_PROPERTIES_BEAN_NAME, mybatisPropertiesBeanDefinition);
    }

    /*
    private SqlSessionFactory registerBeanSqlSessionFactory(
        String keyName, MybatisProperties mybatisProperties, BeanDefinitionRegistry registry) {
      DataSource dataSource;
      try {
        dataSource = beanFactory.getBean(keyName + "_datasource", DataSource.class);
      } catch (NoSuchBeanDefinitionException e) {
        throw new MybatisMultipleException("datasource not found.");
      }

      SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
      factory.setDataSource(dataSource);
      if (mybatisProperties.getConfiguration() == null
          || mybatisProperties.getConfiguration().getVfsImpl() == null) {
        factory.setVfs(SpringBootVFS.class);
      }

      // 以下エラーになるため一旦別の方法で取得
      // ResourceLoader resourceLoader = beanFactory.getBean(ResourceLoader.class);
      if (StringUtils.hasText(mybatisProperties.getConfigLocation())) {
        // factory.setConfigLocation(
        //     resourceLoader.getResource(mybatisProperties.getConfigLocation()));
        // TDOO: 以下は暫定
        try {
          factory.setConfigLocation(
              new PathMatchingResourcePatternResolver().getResource(mybatisProperties.getConfigLocation()));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      factory.setConfiguration(new org.apache.ibatis.session.Configuration());
      if (mybatisProperties.getConfigurationProperties() != null) {
        factory.setConfigurationProperties(mybatisProperties.getConfigurationProperties());
      }
      if (StringUtils.hasLength(mybatisProperties.getTypeAliasesPackage())) {
        factory.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage());
      }
      if (mybatisProperties.getTypeAliasesSuperType() != null) {
        factory.setTypeAliasesSuperType(mybatisProperties.getTypeAliasesSuperType());
      }
      if (StringUtils.hasLength(mybatisProperties.getTypeHandlersPackage())) {
        factory.setTypeHandlersPackage(mybatisProperties.getTypeHandlersPackage());
      }
      Resource[] mapperLocations = mybatisProperties.resolveMapperLocations();
      if (!ObjectUtils.isEmpty(mapperLocations)) {
        factory.setMapperLocations(mapperLocations);
      }
      Set<String> factoryPropertyNames =
          Stream.of(new BeanWrapperImpl(SqlSessionFactoryBean.class).getPropertyDescriptors())
              .map(PropertyDescriptor::getName)
              .collect(Collectors.toSet());
      Class<? extends LanguageDriver> defaultLanguageDriver =
          mybatisProperties.getDefaultScriptingLanguageDriver();
      if (factoryPropertyNames.contains("defaultScriptingLanguageDriver")) {
        factory.setDefaultScriptingLanguageDriver(defaultLanguageDriver);
      }

      BeanDefinitionBuilder sqlSessionFactoryBuild =
          BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactoryBean.class);
      GenericBeanDefinition sqlSessionBeanDefinition =
          (GenericBeanDefinition) sqlSessionFactoryBuild.getBeanDefinition();
      sqlSessionBeanDefinition.getConstructorArgumentValues().addGenericArgumentValue(factory);

      registry.registerBeanDefinition(
          keyName + SUFFIXED_SQL_SESSION_FACTORY_PROPERTIES_BEAN_NAME, sqlSessionBeanDefinition);

      try {
        return factory.getObject();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
     */
    private SqlSessionFactory registerBeanSqlSessionFactory(
        String keyName, MybatisProperties mybatisProperties, BeanDefinitionRegistry registry) {
      DataSource dataSource;
      try {
        dataSource = beanFactory.getBean(keyName + "_datasource", DataSource.class);
      } catch (NoSuchBeanDefinitionException e) {
        throw new MybatisMultipleException("datasource not found.");
      }

      SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
      BeanDefinitionBuilder sqlSessionFactoryBuild =
          BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactoryBean.class);

      factory.setDataSource(dataSource);
      sqlSessionFactoryBuild.addPropertyValue("dataSource", dataSource);
      if (mybatisProperties.getConfiguration() == null
          || mybatisProperties.getConfiguration().getVfsImpl() == null) {
        factory.setVfs(SpringBootVFS.class);
        sqlSessionFactoryBuild.addPropertyValue("vfs", SpringBootVFS.class);
      }

      // 以下エラーになるため一旦別の方法で取得
      // ResourceLoader resourceLoader = beanFactory.getBean(ResourceLoader.class);
      if (StringUtils.hasText(mybatisProperties.getConfigLocation())) {
        // factory.setConfigLocation(
        //     resourceLoader.getResource(mybatisProperties.getConfigLocation()));
        // TDOO: 以下は暫定
        try {
          factory.setConfigLocation(
              new PathMatchingResourcePatternResolver()
                  .getResource(mybatisProperties.getConfigLocation()));
          sqlSessionFactoryBuild.addPropertyValue(
              "configLocation",
              new PathMatchingResourcePatternResolver()
                  .getResource(mybatisProperties.getConfigLocation()));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      factory.setConfiguration(new org.apache.ibatis.session.Configuration());
      sqlSessionFactoryBuild.addPropertyValue(
          "configuration", new org.apache.ibatis.session.Configuration());
      if (mybatisProperties.getConfigurationProperties() != null) {
        factory.setConfigurationProperties(mybatisProperties.getConfigurationProperties());
        sqlSessionFactoryBuild.addPropertyValue(
            "configurationProperties", mybatisProperties.getConfigurationProperties());
      }
      if (StringUtils.hasLength(mybatisProperties.getTypeAliasesPackage())) {
        factory.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage());
        sqlSessionFactoryBuild.addPropertyValue(
            "typeAliasesPackage", mybatisProperties.getTypeAliasesPackage());
      }
      if (mybatisProperties.getTypeAliasesSuperType() != null) {
        factory.setTypeAliasesSuperType(mybatisProperties.getTypeAliasesSuperType());
        sqlSessionFactoryBuild.addPropertyValue(
            "typeAliasesSuperType", mybatisProperties.getTypeAliasesSuperType());
      }
      if (StringUtils.hasLength(mybatisProperties.getTypeHandlersPackage())) {
        factory.setTypeHandlersPackage(mybatisProperties.getTypeHandlersPackage());
        sqlSessionFactoryBuild.addPropertyValue(
            "typeHandlersPackage", mybatisProperties.getTypeHandlersPackage());
      }
      Resource[] mapperLocations = mybatisProperties.resolveMapperLocations();
      if (!ObjectUtils.isEmpty(mapperLocations)) {
        factory.setMapperLocations(mapperLocations);
        sqlSessionFactoryBuild.addPropertyValue("mapperLocations", mapperLocations);
      }
      Set<String> factoryPropertyNames =
          Stream.of(new BeanWrapperImpl(SqlSessionFactoryBean.class).getPropertyDescriptors())
              .map(PropertyDescriptor::getName)
              .collect(Collectors.toSet());
      Class<? extends LanguageDriver> defaultLanguageDriver =
          mybatisProperties.getDefaultScriptingLanguageDriver();
      if (factoryPropertyNames.contains("defaultScriptingLanguageDriver")) {
        factory.setDefaultScriptingLanguageDriver(defaultLanguageDriver);
        sqlSessionFactoryBuild.addPropertyValue(
            "defaultScriptingLanguageDriver", defaultLanguageDriver);
      }

      registry.registerBeanDefinition(
          keyName + SUFFIXED_SQL_SESSION_FACTORY_PROPERTIES_BEAN_NAME,
          sqlSessionFactoryBuild.getBeanDefinition());

      try {
        return factory.getObject();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private void registerBeanSqlSessionTemplate(
        String keyName,
        MybatisProperties mybatisProperties,
        SqlSessionFactory sqlSessionFactory,
        BeanDefinitionRegistry registry) {
      ExecutorType executorType = mybatisProperties.getExecutorType();
      SqlSessionTemplate sqlSessionTemplate;
      if (executorType != null) {
        sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory, executorType);
      } else {
        sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
      }

      BeanDefinitionBuilder sqlSessionTemplateFactoryBuild =
          BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplateFactoryBean.class);
      GenericBeanDefinition sqlSessionTemplateBeanDefinition =
          (GenericBeanDefinition) sqlSessionTemplateFactoryBuild.getBeanDefinition();
      sqlSessionTemplateBeanDefinition
          .getConstructorArgumentValues()
          .addGenericArgumentValue(sqlSessionTemplate);

      registry.registerBeanDefinition(
          keyName + SUFFIXED_SQL_SESSION_TEMPLATE_PROPERTIES_BEAN_NAME,
          sqlSessionTemplateBeanDefinition);
    }
  }
}
