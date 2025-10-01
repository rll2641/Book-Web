package com.fastcampus.book_bot.repository;

import com.fastcampus.book_bot.domain.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    Optional<Book> findByBookIsbn(String bookIsbn);
    Page<Book> findByBookNameContaining(String bookTitle, Pageable pageable);
    Page<Book> findByBookAuthorContaining(String bookAuthor, Pageable pageable);
    Page<Book> findByBookPublisherContaining(String bookPublisher, Pageable pageable);
    Page<Book> findByBookNameContainingOrBookAuthorContainingOrBookPublisherContaining(String bookTitle, String bookAuthor, String bookPublisher, Pageable pageable);

    @Modifying
    @Query("UPDATE Book b SET b.bookQuantity = :newQuantity WHERE b.bookId = :bookId")
    void updateBookQuantity(@Param("bookId") Integer bookId, @Param("newQuantity") Integer newQuantity);


    @Query("""
        SELECT b
        FROM Book b
        JOIN OrderBook ob ON b.bookId = ob.book.bookId
        GROUP BY b.bookId
        order by SUM(ob.quantity) DESC
        LIMIT :limit
    """)
    List<Book> findByTop20ByOrderCount(@Param("limit") int limit);

}