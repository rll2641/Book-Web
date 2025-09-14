package com.fastcampus.book_bot.repository;

import com.fastcampus.book_bot.domain.noti.NotificationSub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationSubRepository extends JpaRepository<NotificationSub, Integer> {
    List<NotificationSub> findActiveByBookIdAndThresholdQuantity(Integer bookId, Integer thresholdQuantity);
}
