package com.fastcampus.book_bot.service.order;

import com.fastcampus.book_bot.repository.OrderBookRepository;
import com.fastcampus.book_bot.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderBookRepository orderBookRepository;


}
