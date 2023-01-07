package com.example;

import com.example.first.UserEntity;
import com.example.first.UserRepository;
import com.example.second.BookEntity;
import com.example.second.BookRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
    UserEntity user = userRepository.getUser(2);
    System.out.println(user.getName());
    BookEntity book = bookRepository.getBook("2");
    return user.getName() + " : " + book.getName();
  }

  @PostMapping
  @Transactional(transactionManager = "first_transaction_manager")
  public void insert() {
    UserEntity user = new UserEntity(2, "hanako");
    BookEntity book = new BookEntity(2, "Effective");

    boolean isError = true;

    userRepository.insertUser(user);
    if (isError) throw new RuntimeException();
    bookRepository.insertBook(book);
  }
}
