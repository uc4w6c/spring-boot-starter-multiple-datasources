package com.example.second;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(
    basePackages = "com.example.second",
    sqlSessionTemplateRef = "second_sql_session_template")
public class MybatisSecondConfiguration {}
