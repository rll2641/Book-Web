package com.fastcampus.book_bot.service.noti;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.domain.noti.NotificationSub;
import com.fastcampus.book_bot.repository.BookRepository;
import com.fastcampus.book_bot.repository.NotificationSubRepository;
import com.fastcampus.book_bot.service.auth.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// 구체적인 관찰 대상 (Spring Component에서 제외하고 직접 인스턴스 생성)
// BookId가 런타임(주문)때 결정되므로 @RequiredArgsConstructor는 사용불가
@Slf4j
public class BookStockManager extends StockSubject {

    private final Integer bookId;
    private final BookRepository bookRepository;
    private final NotificationSubRepository notificationSubRepository;
    private final MailService mailService;

    // 단일 생성자
    public BookStockManager(Integer bookId, BookRepository bookRepository,
                            NotificationSubRepository notificationSubRepository,
                            MailService mailService) {
        this.bookId = bookId;
        this.bookRepository = bookRepository;
        this.notificationSubRepository = notificationSubRepository;
        this.mailService = mailService;
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

        // DB에서 해당 책의 활성 구독자들 중 임계값 조건에 맞는 사용자들 조회
        List<NotificationSub> subList =
                notificationSubRepository.findActiveByBookIdAndThresholdQuantity(bookId, currentStock);

        log.info("재고 알림 대상자 조회 - 도서ID: {}, 현재재고: {}, 알림대상: {}명",
                bookId, currentStock, subList.size());

        // 각 구독자들에게 알림
        for (NotificationSub notificationSub : subList) {
            SubscriptionObserver observer = new SubscriptionObserver(
                    notificationSub.getId(),
                    notificationSubRepository,
                    bookRepository,
                    mailService
            );

            observer.update(this);
        }
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