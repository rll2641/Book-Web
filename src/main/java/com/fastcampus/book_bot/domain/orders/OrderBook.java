package com.fastcampus.book_bot.domain.orders;

import com.fastcampus.book_bot.domain.book.Book;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_book")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderBook {

    /**
     * 주문 도서 ID (Primary Key)
     * 주문상품을 고유하게 식별하는 자동 증가 값
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_BOOK_ID")
    private Integer orderBookId;

    /**
     * 주문 수량
     * 해당 도서를 몇 권 주문했는지
     */
    @Column(name = "QUANTITY")
    private Integer quantity;

    /**
     * 주문 당시 가격
     * 주문 시점의 도서 가격을 저장
     * 가격 변동이 있어도 주문 당시 가격 유지
     */
    @Column(name = "PRICE")
    private Integer price;

    /**
     * 소속 주문
     * N:1 관계 - 여러 주문상품이 한 주문에 속함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Orders order;

    /**
     * 주문된 도서
     * N:1 관계 - 여러 주문상품이 같은 도서를 참조할 수 있음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOK_ID", nullable = false)
    private Book book;
}
