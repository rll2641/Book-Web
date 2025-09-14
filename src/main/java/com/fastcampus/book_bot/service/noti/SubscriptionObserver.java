package com.fastcampus.book_bot.service.noti;

import com.fastcampus.book_bot.domain.noti.NotificationSub;
import com.fastcampus.book_bot.repository.BookRepository;
import com.fastcampus.book_bot.repository.NotificationSubRepository;
import com.fastcampus.book_bot.service.auth.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

// 구체적인 관찰자
@RequiredArgsConstructor
@Slf4j
public class SubscriptionObserver implements StockObserver {

    private final Integer subscriptionId;
    private final NotificationSubRepository notificationSubRepository;
    private final BookRepository bookRepository;
    private final MailService mailService;

    @Override
    public void update(StockSubject subject) {

        Optional<NotificationSub> subOptional = notificationSubRepository.findById(subscriptionId);

        if (subOptional.isEmpty()) {
            log.warn("구독 정보를 찾을 수 없습니다. ID: {}", subscriptionId);
            return;
        }

        NotificationSub notificationSub = subOptional.get();

        // 구독이 비활성화되어 있으면 알림하지 않음
        if (!notificationSub.getIsActive()) {
            log.info("비활성화된 구독 - 알림 생략. 구독ID: {}", subscriptionId);
            return;
        }

        int currentStock = subject.getCurrentStock();
        int bookId = subject.getBookId();
        String bookTitle = subject.getBookTitle();

        // 임계값 조건 확인 - 현재 재고가 설정한 임계값 이하일 때만 알림
        if (currentStock <= notificationSub.getThresholdQuantity()) {

            String message = createNotificationMessage(bookTitle, currentStock);

            try {
                // 이메일 발송
                if (mailService != null) {
                    mailService.sendStockNotification(notificationSub.getUser().getUserEmail(), bookTitle, currentStock, message);
                    log.info("재고 알림 이메일 발송 완료 - 사용자: {}, 도서: {}, 재고: {}권",
                            notificationSub.getUser().getUserEmail(), bookTitle, currentStock);
                } else {
                    log.warn("MailService가 null입니다. 이메일을 발송할 수 없습니다.");
                }
            } catch (Exception e) {
                log.error("재고 알림 이메일 발송 실패 - 사용자: {}, 도서: {}",
                        notificationSub.getUser().getUserEmail(), bookTitle, e);
            }

        } else {
            log.debug("임계값 조건 미충족 - 현재재고: {}, 임계값: {}",
                    currentStock, notificationSub.getThresholdQuantity());
        }
    }

    private String createNotificationMessage(String bookTitle, int currentStock) {
        if (currentStock == 0) {
            return "⚠️ '" + bookTitle + "' 품절되었습니다!";
        } else if (currentStock <= 3) {
            return "🔥 '" + bookTitle + "' 재고 부족! 남은 수량: " + currentStock + "권";
        } else {
            return "📢 '" + bookTitle + "' 재고 알림: " + currentStock + "권 남음";
        }
    }
}