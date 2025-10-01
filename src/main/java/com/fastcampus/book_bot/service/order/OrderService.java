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
import com.fastcampus.book_bot.service.book.BookCacheService;
import com.fastcampus.book_bot.service.grade.GradeStrategy;
import com.fastcampus.book_bot.service.grade.GradeStrategyFactory;
import com.fastcampus.book_bot.service.noti.BookStockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
    private final BookCacheService bookCacheService;
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
               user.getPoint()
        );
    }

    /**
     * 주문 금액 계산
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

            Book book;
            boolean isRedis = false;

            Integer currentStock = bookCacheService.getBookQuantity(ordersDTO.getBookId());

            if (currentStock != null) {
                isRedis = true;
                book = bookCacheService.getBook(ordersDTO.getBookId());
                log.info("Redis 사용 - BookId: {}", ordersDTO.getBookId());

                if (currentStock < ordersDTO.getQuantity()) {
                    log.error("재고 부족 - 요청수량: {}, 현재재고: {}", ordersDTO.getQuantity(), currentStock);
                    throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + currentStock);
                }

                bookCacheService.decrementBookQuantity(ordersDTO.getBookId(), ordersDTO.getQuantity());
            } else {
                log.info("Redis 캐시 미스 - DB 조회 시작 - 도서ID: {}", ordersDTO.getBookId());
                book = bookRepository.findById(ordersDTO.getBookId())
                        .orElseThrow(() -> {
                            log.error("도서 조회 실패 - 존재하지 않는 도서ID: {}", ordersDTO.getBookId());
                            return new IllegalArgumentException("존재하지 않는 도서입니다: " + ordersDTO.getBookId());
                        });
                log.info("DB 조회 완료 - 도서명: {}, 현재재고: {}", book.getBookName(), book.getBookQuantity());

                if (book.getBookQuantity() < ordersDTO.getQuantity()) {
                    log.error("재고 부족 - 요청수량: {}, 현재재고: {}", ordersDTO.getQuantity(), book.getBookQuantity());
                    throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + book.getBookQuantity());
                }
            }

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

            OrderBook orderBook = OrderBook.builder()
                    .order(savedOrder)
                    .book(book)
                    .quantity(ordersDTO.getQuantity())
                    .price(ordersDTO.getPrice())
                    .build();

            OrderBook savedOrderBook = orderBookRepository.save(orderBook);
            log.info("주문상품 저장 성공 - 주문상품ID: {}, 수량: {}, 가격: {}",
                    savedOrderBook.getOrderBookId(), savedOrderBook.getQuantity(), savedOrderBook.getPrice());

            updateStockAsync(book.getBookId(), ordersDTO.getQuantity());
            updateStockAndNotify(book.getBookId(), ordersDTO.getQuantity());

            log.info("=== 주문 저장 프로세스 완료 (Redis 캐시 히트: {}) ===", isRedis);

        } catch (Exception e) {
            log.error("주문 저장 중 오류 발생", e);
            log.error("오류 상세 정보 - 사용자ID: {}, 상품ID: {}, 오류메시지: {}",
                    user.getUserId(), ordersDTO.getBookId(), e.getMessage());
            throw e;
        }
    }

    /**
     * DB 재고 차감 (비동기)
     */
    @Async
    public void updateStockAsync(Integer bookId, Integer orderQuantity) {

        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다: " + bookId));

            Integer newQuantity = book.getBookQuantity() - orderQuantity;
            log.info("재고 업데이트 시작 (비동기) - 도서: {}, 기존재고: {}, 주문수량: {}, 새재고: {}",
                    book.getBookName(), book.getBookQuantity(), orderQuantity, newQuantity);

            bookRepository.updateBookQuantity(bookId, newQuantity);

            log.info("DB 재고 업데이트 완료 (비동기) - 도서ID: {}", bookId);

        } catch (Exception e) {
            log.error("DB 재고 업데이트 중 오류 발생 (비동기) - 도서ID: {}", bookId, e);
        }
    }

    /**
     * 재고 업데이트 및 알림 처리
     */
    @Transactional
    public void updateStockAndNotify(Integer bookId, Integer orderQuantity) {
        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다: " + bookId));

            Integer newQuantity = book.getBookQuantity() - orderQuantity;
            log.info("재고 업데이트 시작 - 도서: {}, 기존재고: {}, 주문수량: {}, 새재고: {}",
                    book.getBookName(), book.getBookQuantity(), orderQuantity, newQuantity);

            BookStockManager stockManager = new BookStockManager(
                    bookId,
                    bookRepository,
                    notificationSubRepository,
                    mailService
            );

            stockManager.updateStock(newQuantity);

            log.info("재고 업데이트 및 알림 처리 완료 - 도서ID: {}", bookId);

        } catch (Exception e) {
            log.error("재고 업데이트 및 알림 처리 중 오류 발생 - 도서ID: {}", bookId, e);
            throw e;
        }
    }
}