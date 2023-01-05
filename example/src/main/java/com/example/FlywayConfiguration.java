package com.example;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class FlywayConfiguration {
  @Bean(initMethod = "start", destroyMethod = "stop")
  public Server inMemoryH2DatabaseaServer() throws SQLException {
    return Server.createTcpServer(
        "-tcp", "-tcpAllowOthers", "-tcpPort", "9090");
  }

  @Bean(name = "firstFlyway", initMethod = "migrate")
  public Flyway createFirstFlyway(@Qualifier("first_datasource") DataSource dataSource) {
    return new Flyway(
        new FluentConfiguration().locations("db/migration/first").dataSource(dataSource));
  }
}
