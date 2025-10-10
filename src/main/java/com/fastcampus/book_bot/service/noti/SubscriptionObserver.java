package com.fastcampus.book_bot.service.noti;

import com.fastcampus.book_bot.domain.noti.NotificationSub;
import com.fastcampus.book_bot.repository.BookRepository;
import com.fastcampus.book_bot.repository.NotificationSubRepository;
import com.fastcampus.book_bot.service.auth.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class SubscriptionObserver implements StockObserver {

    private final Integer subscriptionId;
    private final String userEmail;
    private final Integer thresholdQuantity;
    private final Boolean isActive;
    private final MailService mailService;

    @Override
    public void update(StockSubject subject) {
        try {
            if (!isActive) {
                log.info("비활성화된 구독 - 알림 생략. 구독ID: {}", subscriptionId);
                return;
            }

            int currentStock = subject.getCurrentStock();
            String bookTitle = subject.getBookTitle();

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
            log.error("재고 알림 이메일 발송 실패 - 구독ID: {}, 사용자: {}", subscriptionId, userEmail, e);
        }
    }

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