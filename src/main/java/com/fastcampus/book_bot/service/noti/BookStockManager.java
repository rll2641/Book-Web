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
import java.util.stream.Collectors;

// 구체적인 관찰 대상 (Spring Component에서 제외하고 직접 인스턴스 생성)
// BookId가 런타임(주문)때 결정되므로 @RequiredArgsConstructor는 사용불가
@Slf4j
public class BookStockManager extends StockSubject {

    private final Integer bookId;
    private final BookRepository bookRepository;
    private final NotificationSubRepository notificationSubRepository;
    private final MailService mailService;
    private final ThreadPoolTaskExecutor taskExecutor;

    // 단일 생성자
    public BookStockManager(Integer bookId, BookRepository bookRepository,
                            NotificationSubRepository notificationSubRepository,
                            MailService mailService, ThreadPoolTaskExecutor taskExecutor) {
        this.bookId = bookId;
        this.bookRepository = bookRepository;
        this.notificationSubRepository = notificationSubRepository;
        this.mailService = mailService;
        this.taskExecutor = taskExecutor;
    }

    // 재고 업데이트 (구매, 입고) 등
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
                    Integer subscriptionId = notificationSub.getId();
                    String userEmail = notificationSub.getUser().getUserEmail();
                    Integer thresholdQuantity = notificationSub.getThresholdQuantity();

                    return CompletableFuture.runAsync(() -> {
                        sendNotification(subscriptionId, userEmail, bookTitle, currentStock, thresholdQuantity);
                    }, taskExecutor);
                })
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> log.info("모든 재고 알림 처리 완료 - 도서ID: {}, 처리건수: {}건",
                        bookId, futures.size()))
                .exceptionally(ex -> {
                    log.error("재고 알림 처리 중 오류 발생 - 도서ID: {}", bookId, ex);
                    return null;
                });    }

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

    private void sendNotification(Integer subscriptionId, String userEmail,
                                  String bookTitle, int currentStock, int thresholdQuantity) {
        try {
            if (currentStock <= thresholdQuantity) {
                String message = createNotificationMessage(bookTitle, currentStock);

                if (mailService != null) {
                    mailService.sendStockNotification(userEmail, bookTitle, currentStock, message);
                    log.info("재고 알림 이메일 발송 완료 - 사용자: {}, 도서: {}, 재고: {}권",
                            userEmail, bookTitle, currentStock);
                } else {
                    log.warn("MailService가 null입니다. 이메일을 발송할 수 없습니다.");
                }
            } else {
                log.debug("임계값 조건 미충족 - 현재재고: {}, 임계값: {}",
                        currentStock, thresholdQuantity);
            }
        } catch (Exception e) {
            log.error("재고 알림 이메일 발송 실패 - 사용자: {}, 도서: {}", userEmail, bookTitle, e);
        }
    }

    /**
     * 알림 메시지 생성
     * ← 이 메서드도 새로 추가했습니다!
     */
    private String createNotificationMessage(String bookTitle, int currentStock) {
        if (currentStock == 0) {
            return bookTitle + "' 품절되었습니다!";
        } else if (currentStock <= 3) {
            return bookTitle + "' 재고 부족! 남은 수량: " + currentStock + "권";
        } else {
            return bookTitle + "' 재고 알림: " + currentStock + "권 남음";
        }
    }
}