package com.fastcampus.book_bot.repository;

import com.fastcampus.book_bot.domain.orders.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders, Integer> {

}
