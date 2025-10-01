package com.fastcampus.book_bot.service.noti;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.domain.noti.NotificationSub;
import com.fastcampus.book_bot.repository.BookRepository;
import com.fastcampus.book_bot.repository.NotificationSubRepository;
import com.fastcampus.book_bot.service.auth.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class BookStockManager extends StockSubject {

    private final Integer bookId;
    private final BookRepository bookRepository;
    private final NotificationSubRepository notificationSubRepository;
    private final MailService mailService;
    private final ThreadPoolTaskExecutor taskExecutor;

    public BookStockManager(Integer bookId, BookRepository bookRepository,
                            NotificationSubRepository notificationSubRepository,
                            MailService mailService, ThreadPoolTaskExecutor taskExecutor) {
        this.bookId = bookId;
        this.bookRepository = bookRepository;
        this.notificationSubRepository = notificationSubRepository;
        this.mailService = mailService;
        this.taskExecutor = taskExecutor;
    }

    @Transactional
    public void updateStock(Integer newQuantity) {
        bookRepository.updateBookQuantity(bookId, newQuantity);

        Optional<Book> bookOpt = bookRepository.findById(bookId);
        bookOpt.ifPresent(book -> log.info("[재고 변동] {}: {}권", book.getBookName(), book.getBookQuantity()));

        notifyObservers();
    }

    @Override
    @Transactional
    public void notifyObservers() {
        Integer currentStock = getCurrentStock();

        List<NotificationSub> subList =
                notificationSubRepository.findActiveByBookIdAndThresholdQuantity(bookId, currentStock);

        log.info("재고 알림 대상자 조회 - 도서ID: {}, 현재재고: {}, 알림대상: {}명",
                bookId, currentStock, subList.size());

        String bookTitle = getBookTitle();

        List<CompletableFuture<Void>> futures = subList.stream()
                .map(notificationSub -> {
                    // 트랜잭션 내에서 미리 필요한 데이터를 추출 (Lazy Loading 방지)
                    Integer subscriptionId = notificationSub.getId();
                    String userEmail = notificationSub.getUser().getUserEmail();
                    Integer thresholdQuantity = notificationSub.getThresholdQuantity();
                    Boolean isActive = notificationSub.getIsActive();

                    SubscriptionObserver observer = new SubscriptionObserver(
                            subscriptionId,
                            userEmail,
                            thresholdQuantity,
                            isActive,
                            mailService
                    );

                    return CompletableFuture.runAsync(() -> {
                        observer.update(this);
                    }, taskExecutor);
                })
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> log.info("모든 재고 알림 처리 완료 - 도서ID: {}, 처리건수: {}건",
                        bookId, futures.size()))
                .exceptionally(ex -> {
                    log.error("재고 알림 처리 중 오류 발생 - 도서ID: {}", bookId, ex);
                    return null;
                });
    }

    @Override
    public int getCurrentStock() {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        return bookOpt.map(Book::getBookQuantity).orElse(0);
    }

    @Override
    public int getBookId() {
        return this.bookId;
    }

    @Override
    public String getBookTitle() {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        return bookOpt.map(Book::getBookName).orElse("Unknown");
    }
}