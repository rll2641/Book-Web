package com.fastcampus.book_bot.service.noti;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.repository.BookRepository;
import com.fastcampus.book_bot.repository.NotificationSubRepository;
import com.fastcampus.book_bot.service.auth.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStockService {

    private final BookRepository bookRepository;
    private final NotificationSubRepository notificationSubRepository;
    private final MailService mailService;
    private final ThreadPoolTaskExecutor taskExecutor;

    /**
     * DB 재고 차감 및 알림 처리 (비동기)
     */
    @Transactional
    @Async
    public void updateStockAndNotify(Integer bookId, Integer orderQuantity) {
        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다: " + bookId));

            Integer newQuantity = book.getBookQuantity() - orderQuantity;
            log.info("재고 업데이트 시작 (비동기) - 도서: {}, 기존재고: {}, 주문수량: {}, 새재고: {}",
                    book.getBookName(), book.getBookQuantity(), orderQuantity, newQuantity);

            // BookStockManager를 사용하여 재고 업데이트 및 Observer 패턴으로 알림 발송
            BookStockManager stockManager = new BookStockManager(
                    bookId,
                    bookRepository,
                    notificationSubRepository,
                    mailService,
                    taskExecutor
            );

            stockManager.updateStock(newQuantity);

            log.info("재고 업데이트 및 알림 처리 완료 (비동기) - 도서ID: {}", bookId);

        } catch (Exception e) {
            log.error("재고 업데이트 및 알림 처리 중 오류 발생 (비동기) - 도서ID: {}", bookId, e);
        }
    }
}
