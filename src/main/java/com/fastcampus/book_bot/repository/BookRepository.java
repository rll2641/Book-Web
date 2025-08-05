package com.fastcampus.book_bot.repository;

import com.fastcampus.book_bot.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByBookIsbn(Integer bookIsbn);
}