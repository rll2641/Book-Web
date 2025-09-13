package com.fastcampus.book_bot.service.order;

import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.dto.order.OrderCalculationResult;
import com.fastcampus.book_bot.dto.order.OrdersDTO;
import com.fastcampus.book_bot.repository.OrderBookRepository;
import com.fastcampus.book_bot.repository.OrderRepository;
import com.fastcampus.book_bot.service.grade.GradeStrategy;
import com.fastcampus.book_bot.service.grade.GradeStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final GradeStrategyFactory gradeStrategyFactory;

    /**
     * 주문 금액 계산 (User 객체 기반)
     */
    public OrderCalculationResult calculateOrder(OrdersDTO ordersDTO, User user) {
        return calculateOrder(
                user.getUserGrade().getGradeName(),
                ordersDTO.getPrice() * ordersDTO.getQuantity(),
                0  // 포인트 사용은 기본값 0 (나중에 추가 가능)
        );
    }

    /**
     * 주문 금액 계산 (포인트 사용 포함)
     */
    public OrderCalculationResult calculateOrder(OrdersDTO ordersDTO, User user, Integer usedPoints) {
        return calculateOrder(
                user.getUserGrade().getGradeName(),
                ordersDTO.getPrice() * ordersDTO.getQuantity(),
                usedPoints != null ? usedPoints : 0
        );
    }

    /**
     * 주문 금액 계산 (기본 메서드)
     */
    public OrderCalculationResult calculateOrder(String gradeName, Integer originalAmount, Integer usedPoints) {
        GradeStrategy strategy = gradeStrategyFactory.getStrategy(gradeName);

        // 등급 할인 적용
        Integer discountedPrice = strategy.calculateDiscountedPrice(originalAmount);
        Integer gradeDiscountAmount = originalAmount - discountedPrice;

        // 포인트 사용 적용
        Integer afterPointUsage = Math.max(0, discountedPrice - usedPoints);

        // 배송비 계산
        Integer shippingCost = strategy.calculateShippingCost(afterPointUsage);

        // 최종 결제 금액
        Integer finalAmount = afterPointUsage + shippingCost;

        // 마일리지 적립 (원가 기준)
        Integer earnedMileage = strategy.calculateMileage(originalAmount);

        return OrderCalculationResult.builder()
                .originalAmount(originalAmount)
                .gradeDiscountAmount(gradeDiscountAmount)
                .usedPoints(usedPoints)
                .afterDiscountAmount(discountedPrice)
                .shippingCost(shippingCost)
                .finalAmount(finalAmount)
                .earnedMileage(earnedMileage)
                .gradeName(strategy.getGradeName())
                .build();
    }
}
