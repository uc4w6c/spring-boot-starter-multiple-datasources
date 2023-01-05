package com.example.second;

public class BookEntity {
  private int id;
  private String name;

  public BookEntity(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
