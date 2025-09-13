package com.fastcampus.book_bot.controller.order;

import com.fastcampus.book_bot.common.response.SuccessApiResponse;
import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.dto.order.OrdersDTO;
import com.fastcampus.book_bot.service.order.OrderService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<SuccessApiResponse<Void>> orderComplete(OrdersDTO ordersDTO,
                                                                  HttpServletRequest request) {

        User user = (User) request.getAttribute("currentUser");

        orderService.saveOrder(user, ordersDTO);

        return ResponseEntity.ok(SuccessApiResponse.of("엔티티 저장 완료"));
    }
}
