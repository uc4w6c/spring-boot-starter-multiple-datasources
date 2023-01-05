package com.example;

// import com.github.uc4w6c.boot.autoconfigure.MultipleDataSourcesProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
@RequestMapping("hello")
public class HelloController {
  // private MultipleDataSourcesProperties multipleDataSourcesProperties;
  private DataSourceProperties dataSourceProperties;

  /*public HelloController(MultipleDataSourcesProperties multipleDataSourcesProperties, @Qualifier("aa") DataSourceProperties dataSourceProperties) {
    this.multipleDataSourcesProperties = multipleDataSourcesProperties;
    this.dataSourceProperties = dataSourceProperties;
  }
   */

  public HelloController(@Qualifier("first_datasource_properties") DataSourceProperties dataSourceProperties) {
    this.dataSourceProperties = dataSourceProperties;
  }

  @GetMapping
  public String index() {
    // System.out.println(multipleDataSourcesProperties);
    System.out.println(dataSourceProperties);
    return "Hello!";
  }
}
