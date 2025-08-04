package com.fastcampus.book_bot.domain;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_ID", nullable = false, updatable = false)
    private Long bookId;

    @Column(name = "BOOK_TITLE", length = 100, nullable = false)
    private String bookTitle;

    @Column(name = "BOOK_AUTHOR", length = 200)
    private String bookAuthor;

    @Column(name = "BOOK_LINK", length = 200)
    private String bookLink;

    @Column(name = "BOOK_IMAGE", length = 200)
    private String bookImage;

    @Column(name = "BOOK_PUBLISHER", length = 100)
    private String bookPublisher;

    @Column(name = "BOOK_ISBN")
    private Integer bookIsbn;

    @Column(name = "BOOK_DESCRIPTION", length = 500)
    private String bookDescription;

    @Column(name = "BOOK_PUBDATE")
    private LocalDateTime bookPubdate;

    @Column(name = "BOOK_DISCOUNT")
    private Integer bookDiscount;

    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

}
