package com.example.second;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookRepository {
  BookEntity getBook(@Param("id") String id);
}
