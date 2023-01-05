package com.github.uc4w6c.boot.autoconfigure;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass(DataSource.class)
// @EnableConfigurationProperties(MultipleDataSourcesProperties.class)
@Import(MultipleDataSourcesConfiguration.MultipleDataSourceRegistrar.class)
public class MultipleDataSourcesConfiguration {
  static class MultipleDataSourceRegistrar
      implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static final String DATASOURCES_PREFIX = "spring.datasources";

    private static final String SUFFIXED_DATASOURCE_PROPERTIES_BEAN_NAME = "_datasource_properties";
    private static final String SUFFIXED_DATASOURCE_BEAN_NAME = "_datasource";

    private Map<String, DataSourceProperties> multipleDataSourcesProperties;

    @Override
    public void setEnvironment(Environment environment) {
      BindResult<Map<String, DataSourceProperties>> bind =
          Binder.get(environment)
              .bind(DATASOURCES_PREFIX, Bindable.mapOf(String.class, DataSourceProperties.class));

      multipleDataSourcesProperties =
          bind.orElseThrow(
              () ->
                  new MultipleDataSourcesException(
                      String.format("Please set %s.", DATASOURCES_PREFIX)));
      if (multipleDataSourcesProperties.isEmpty()) {
        throw new MultipleDataSourcesException("Please Set at least one Datasource.");
      }
    }

    @Override
    public void registerBeanDefinitions(
        AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
      // TODO: おそらくTransactionの設定も必要
      for (Map.Entry<String, DataSourceProperties> entry :
          multipleDataSourcesProperties.entrySet()) {
        DataSourceProperties dataSourceProperties = entry.getValue();

        // DataSourceProperties
        BeanDefinitionBuilder dataSourcePropertiesBuild =
            BeanDefinitionBuilder.genericBeanDefinition(DataSourcePropertiesFactoryBean.class);
        GenericBeanDefinition dataSourcePropertiesBeanDefinition =
            (GenericBeanDefinition) dataSourcePropertiesBuild.getBeanDefinition();

        dataSourcePropertiesBeanDefinition
            .getConstructorArgumentValues()
            .addGenericArgumentValue(dataSourceProperties);
        registry.registerBeanDefinition(
            entry.getKey() + SUFFIXED_DATASOURCE_PROPERTIES_BEAN_NAME,
            dataSourcePropertiesBeanDefinition);

        // DataSource
        BeanDefinitionBuilder dataSourceBuild =
            BeanDefinitionBuilder.genericBeanDefinition(DataSourceFactoryBean.class);
        GenericBeanDefinition dataSourceBeanDefinition =
            (GenericBeanDefinition) dataSourceBuild.getBeanDefinition();

        DataSource dataSource = getDataSource(dataSourceProperties);

        dataSourceBeanDefinition
            .getConstructorArgumentValues()
            .addGenericArgumentValue(dataSource);

        // 一旦はデフォルトのdatasourceのみを作成しそれ以外のType指定は別途対応する
        registry.registerBeanDefinition(
            entry.getKey() + SUFFIXED_DATASOURCE_BEAN_NAME, dataSourceBeanDefinition);
      }
    }

    private DataSource getDataSource(DataSourceProperties dataSourceProperties) {
      Function<DataSourceProperties, HikariDataSource> createHikariDataSource = (properties) -> {
        HikariDataSource dataSource = createDataSource(properties, HikariDataSource.class);
        if (StringUtils.hasText(dataSourceProperties.getName())) {
          dataSource.setPoolName(dataSourceProperties.getName());
        }
        return dataSource;
      };

      Class dataSourceTypeClass = dataSourceProperties.getType();
      if (dataSourceTypeClass == null) {
        return createHikariDataSource.apply(dataSourceProperties);
      } else if ("com.zaxxer.hikari.HikariDataSource".equals(dataSourceTypeClass)) {
        return createHikariDataSource.apply(dataSourceProperties);
      } else {
        return createHikariDataSource.apply(dataSourceProperties);
      }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createDataSource(DataSourceProperties properties, Class<? extends DataSource> type) {
      return (T) properties.initializeDataSourceBuilder().type(type).build();
    }
  }

  /*
  public static class MultipleDataSourceRegistrar
      implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private MultipleDataSourcesProperties multipleDataSourcesProperties;

    @Override
    public void setEnvironment(Environment environment) {
      BindResult<MultipleDataSourcesProperties> bind =
          Binder.get(environment)
              .bind(MultipleDataSourcesProperties.PREFIX, MultipleDataSourcesProperties.class);

      multipleDataSourcesProperties =
          bind.orElseThrow(
              () ->
                  new MultipleDataSourcesException(
                      String.format("Please set %s", MultipleDataSourcesProperties.PREFIX)));
      if (multipleDataSourcesProperties.datasources().isEmpty()) {
        throw new MultipleDataSourcesException("Please Set at least one Datasource.");
      }
    }

    @Override
    public void registerBeanDefinitions(
        AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
      for (Map.Entry<String, DataSourceProperties> entry :
          multipleDataSourcesProperties.datasources().entrySet()) {
        BeanDefinitionBuilder dataSourceBuild =
            BeanDefinitionBuilder.genericBeanDefinition(DataSourceFactoryBean.class);
        GenericBeanDefinition dataSourceBeanDefinition =
            (GenericBeanDefinition) dataSourceBuild.getBeanDefinition();
        dataSourceBeanDefinition
            .getConstructorArgumentValues()
            .addGenericArgumentValue(entry.getValue());

        // TODO: Bean名は"xx_datasource"に変えるかも
        registry.registerBeanDefinition(entry.getKey(), dataSourceBeanDefinition);
      }
    }
  }
   */
  /*
   public static class MultipleDataSourceRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {
     private BeanFactory beanFactory;

     @Override
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
       this.beanFactory = beanFactory;
     }

     @Override
     public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
       MultipleDataSourcesProperties multipleDataSourcesProperties = beanFactory.getBean(MultipleDataSourcesProperties.class.getName(), MultipleDataSourcesProperties.class);
       for (Map.Entry<String, DataSourceProperties> entry : multipleDataSourcesProperties.datasource().entrySet()) {
         BeanDefinitionBuilder dataSourceBuild = BeanDefinitionBuilder.genericBeanDefinition(DataSourceFactoryBean.class);
         GenericBeanDefinition dataSourceBeanDefinition = (GenericBeanDefinition) dataSourceBuild.getBeanDefinition();
         dataSourceBeanDefinition.getConstructorArgumentValues().addGenericArgumentValue(entry.getValue());

         registry.registerBeanDefinition(entry.getKey(), dataSourceBeanDefinition);
       }
     }
   }
  */
}
