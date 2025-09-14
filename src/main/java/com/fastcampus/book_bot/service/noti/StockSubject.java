package com.fastcampus.book_bot.service.noti;

public abstract class StockSubject {

    // 모든 Observer에게 알림
    public abstract void notifyObservers();

    // 현재 상태를 가져오기
    public abstract int getCurrentStock();
    public abstract int getBookId();
    public abstract String getBookTitle();

}
