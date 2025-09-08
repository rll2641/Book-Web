package com.fastcampus.book_bot.controller.order;

import com.fastcampus.book_bot.dto.order.OrdersDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OrderViewController {

    @PostMapping("/order")
    public String order(@ModelAttribute("orderForm") OrdersDTO ordersDTO) {
        return "order/order";
    }

}
