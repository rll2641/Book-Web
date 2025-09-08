package com.fastcampus.book_bot.controller.order;

import com.fastcampus.book_bot.common.response.SuccessApiResponse;
import com.fastcampus.book_bot.dto.order.OrdersDTO;
import com.fastcampus.book_bot.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/complete")
    public ResponseEntity<SuccessApiResponse<Void>> orderComplete(OrdersDTO ordersDTO) {

        orderService.orderBook(ordersDTO);

        return ResponseEntity.ok(SuccessApiResponse.of(""));
    }
}
