package com.fastcampus.book_bot.repository;

import com.fastcampus.book_bot.domain.orders.OrderBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderBookRepository extends JpaRepository<OrderBook, Integer> {
}
