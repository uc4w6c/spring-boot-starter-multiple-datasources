package com.example;

import com.example.first.UserEntity;
import com.example.first.UserRepository;
import com.example.second.BookEntity;
import com.example.second.BookRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("db")
public class DatabaseController {
  private UserRepository userRepository;
  private BookRepository bookRepository;

  public DatabaseController(UserRepository userRepository, BookRepository bookRepository) {
    this.userRepository = userRepository;
    this.bookRepository = bookRepository;
  }

  @GetMapping
  public String index() {
    UserEntity user = userRepository.getUser(1);
    BookEntity book = bookRepository.getBook("1");
    return user.getName() + " : " + book.getName();
  }
}
