# Spring Boot Starter Multiple DataSources
Spring Boot Starter Multiple DataSources helps you work with multiple datasources.

## Installing
### Gradle
```groovy
dependencies {
    implementation platform('com.github.uc4w6c:spring-boot-starter-multiple-datasources:1.0.0')
    implementation 'com.github.uc4w6c:spring-boot-starter-multiple-datasources-mybatis'
}
```

## Using

### Common
Configure `spring.datasources` and `spring.transactions` for each datasource.

application.yaml

```yaml:
spring:
  datasources:
    first:
      url: jdbc:h2:mem:mydb
      username: sa
      password:
      driverClassName: org.h2.Driver
    second:
      url: jdbc:h2:tcp://localhost:9090/mem:mydb
      username: sa
      password:
      driverClassName: org.h2.Driver
  transactions:
    first:
      defaultTimeout: 30
      rollbackOnCommitFailure: true
```

### Mybatis
Configure `mybatis` for each datasource.

application.yaml

```yaml
mybatis:
  first:
    mapper-locations: classpath*:/com/example/first/*.xml
  second:
    mapper-locations: classpath*:/com/example/second/*.xml
```

Configure only MapperScan for each data source.

```java
@Configuration
@MapperScan(
    basePackages = "com.example.first",
    sqlSessionTemplateRef = "first_sql_session_template")
public class MybatisFirstConfiguration {}

@Configuration
@MapperScan(
    basePackages = "com.example.second",
    sqlSessionTemplateRef = "second_sql_session_template")
public class MybatisSecondConfiguration {}
```
