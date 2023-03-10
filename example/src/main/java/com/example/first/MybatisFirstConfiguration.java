package com.example.first;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(
    basePackages = "com.example.first",
    sqlSessionTemplateRef = "first_sql_session_template")
public class MybatisFirstConfiguration {}
