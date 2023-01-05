package com.example.first;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRepository {
  UserEntity getUser(@Param("id") int id);
}
