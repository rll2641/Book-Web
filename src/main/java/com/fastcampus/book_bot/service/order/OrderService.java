package com.fastcampus.book_bot.service.order;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.domain.orders.OrderBook;
import com.fastcampus.book_bot.domain.orders.Orders;
import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.dto.order.OrderCalculationResult;
import com.fastcampus.book_bot.dto.order.OrdersDTO;
import com.fastcampus.book_bot.repository.BookRepository;
import com.fastcampus.book_bot.repository.OrderBookRepository;
import com.fastcampus.book_bot.repository.OrderRepository;
import com.fastcampus.book_bot.repository.NotificationSubRepository;
import com.fastcampus.book_bot.service.auth.MailService;
import com.fastcampus.book_bot.service.grade.GradeStrategy;
import com.fastcampus.book_bot.service.grade.GradeStrategyFactory;
import com.fastcampus.book_bot.service.noti.BookStockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final GradeStrategyFactory gradeStrategyFactory;
    private final OrderBookRepository orderBookRepository;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final NotificationSubRepository notificationSubRepository;
    private final MailService mailService;

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

    @Transactional
    public void saveOrder(User user, OrdersDTO ordersDTO) {

        try {
            OrderCalculationResult calculationResult = calculateOrder(ordersDTO, user);
            log.info("주문 금액 계산 완료 - 최종 결제금액: {}", calculationResult.getFinalAmount());

            Book book = bookRepository.findById(ordersDTO.getBookId().intValue())
                    .orElseThrow(() -> {
                        log.error("도서 조회 실패 - 존재하지 않는 도서ID: {}", ordersDTO.getBookId());
                        return new IllegalArgumentException("존재하지 않는 도서입니다: " + ordersDTO.getBookId());
                    });
            log.info("도서 조회 성공 - 도서명: {}, 재고: {}", book.getBookName(), book.getBookQuantity());

            if (book.getBookQuantity() < ordersDTO.getQuantity()) {
                log.error("재고 부족 - 요청수량: {}, 현재재고: {}", ordersDTO.getQuantity(), book.getBookQuantity());
                throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + book.getBookQuantity());
            }

            // 주문 정보 저장
            Orders order = Orders.builder()
                    .user(user)
                    .orderStatus("ORDER_READY")
                    .totalPrice(calculationResult.getFinalAmount())
                    .orderDay(LocalDate.now())
                    .orderDate(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            Orders savedOrder = orderRepository.save(order);
            log.info("주문 저장 성공 - 주문ID: {}, 상태: {}, 총금액: {}",
                    savedOrder.getOrderId(), savedOrder.getOrderStatus(), savedOrder.getTotalPrice());

            // 주문 상품 정보 저장
            OrderBook orderBook = OrderBook.builder()
                    .order(savedOrder)
                    .book(book)
                    .quantity(ordersDTO.getQuantity())
                    .price(ordersDTO.getPrice())
                    .build();

            OrderBook savedOrderBook = orderBookRepository.save(orderBook);
            log.info("주문상품 저장 성공 - 주문상품ID: {}, 수량: {}, 가격: {}",
                    savedOrderBook.getOrderBookId(), savedOrderBook.getQuantity(), savedOrderBook.getPrice());

            // ===== 재고 업데이트 및 알림 처리 =====
            updateStockAndNotify(book.getBookId(), ordersDTO.getQuantity());

            log.info("=== 주문 저장 프로세스 완료 ===");

        } catch (Exception e) {
            log.error("주문 저장 중 오류 발생", e);
            log.error("오류 상세 정보 - 사용자ID: {}, 상품ID: {}, 오류메시지: {}",
                    user.getUserId(), ordersDTO.getBookId(), e.getMessage());
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }

    /**
     * 재고 업데이트 및 알림 처리
     */
    @Transactional
    public void updateStockAndNotify(Integer bookId, Integer orderQuantity) {
        try {
            // 현재 재고 조회
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다: " + bookId));

            // 새로운 재고 수량 계산
            Integer newQuantity = book.getBookQuantity() - orderQuantity;
            log.info("재고 업데이트 시작 - 도서: {}, 기존재고: {}, 주문수량: {}, 새재고: {}",
                    book.getBookName(), book.getBookQuantity(), orderQuantity, newQuantity);

            // BookStockManager를 통해 재고 업데이트 및 알림 발송
            BookStockManager stockManager = new BookStockManager(
                    bookId,
                    bookRepository,
                    notificationSubRepository,
                    mailService
            );

            // 재고 업데이트 (이 메서드 내부에서 알림도 자동으로 발송됨)
            stockManager.updateStock(newQuantity);

            log.info("재고 업데이트 및 알림 처리 완료 - 도서ID: {}", bookId);

        } catch (Exception e) {
            log.error("재고 업데이트 및 알림 처리 중 오류 발생 - 도서ID: {}", bookId, e);
            throw e;
        }
    }
}