package com.fastcampus.book_bot.controller.order;

import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.dto.order.OrderCalculationResult;
import com.fastcampus.book_bot.dto.order.OrdersDTO;
import com.fastcampus.book_bot.service.grade.GradeStrategyFactory;
import com.fastcampus.book_bot.service.order.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class OrderViewController {

    private final OrderService orderService;

    @PostMapping("/order")
    public String order(@ModelAttribute("orderForm") OrdersDTO ordersDTO,
                        HttpServletRequest request,
                        Model model) {

        User user = (User) request.getAttribute("currentUser");
        OrderCalculationResult result = orderService.calculateOrder(ordersDTO, user);

        model.addAttribute("calculationResult", result);
        model.addAttribute("currentUser", user);
        model.addAttribute("userGrade", user.getUserGrade());
        model.addAttribute("userPoints", user.getPoint());

        return "order/order";
    }
}
