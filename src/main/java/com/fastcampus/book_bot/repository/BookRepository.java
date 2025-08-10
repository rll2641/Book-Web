package com.fastcampus.book_bot.repository;

import com.fastcampus.book_bot.domain.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByBookIsbn(String bookIsbn);
    Page<Book> findByBookNameContaining(String bookTitle, Pageable pageable);
    Page<Book> findByBookAuthorContaining(String bookAuthor, Pageable pageable);
    Page<Book> findByBookPublisherContaining(String bookPublisher, Pageable pageable);
    Page<Book> findByBookNameContainingOrBookAuthorContainingOrBookPublisherContaining(String bookTitle, String bookAuthor, String bookPublisher, Pageable pageable);

}