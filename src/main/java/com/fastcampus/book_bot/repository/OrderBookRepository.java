package com.fastcampus.book_bot.repository;

import com.fastcampus.book_bot.domain.orders.OrderBook;
import com.fastcampus.book_bot.dto.book.BookSalesDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderBookRepository extends JpaRepository<OrderBook, Integer> {

        @Query("""
            SELECT new com.fastcampus.book_bot.dto.book.BookSalesDTO(
                ob.book.bookId, SUM(ob.quantity)
            )
            FROM OrderBook ob
            JOIN ob.order o
            WHERE o.orderDate >= :weekAgo
            GROUP BY ob.book.bookId
            ORDER BY SUM(ob.quantity) DESC
            """)
        List<BookSalesDTO> findWeeklyBestSellers(@Param("weekAgo") LocalDateTime weekAgo);

        @Query("""
            SELECT new com.fastcampus.book_bot.dto.book.BookSalesDTO(
                ob.book.bookId, SUM(ob.quantity))
            FROM OrderBook ob
            JOIN ob.order o
            WHERE o.orderDate >= :monthAgo
            GROUP BY ob.book.bookId
            ORDER BY SUM(ob.quantity) DESC
            """)
        List<BookSalesDTO> findMonthlyBestSellers(@Param("monthAgo") LocalDateTime monthAgo);
    }

