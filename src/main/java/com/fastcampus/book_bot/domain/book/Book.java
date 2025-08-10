package com.fastcampus.book_bot.domain.book;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "`BOOKS`")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOK_ID", nullable = false, updatable = false)
    private Integer bookId;

    @Column(name = "BOOK_NAME", length = 300)
    private String bookName;

    @Column(name = "BOOK_AUTHOR", length = 300)
    private String bookAuthor;

    @Column(name = "BOOK_PUBLISHER", length = 300)
    private String bookPublisher;

    @Column(name = "BOOK_DESCRIPTION", length = 3000)
    private String bookDescription;

    @Column(name = "BOOK_PUBDATE")
    private LocalDate bookPubdate;

    @Column(name = "BOOK_DISCOUNT")
    private Integer bookDiscount;

    @Column(name = "BOOK_LINK")
    private String bookLink;

    @Column(name = "BOOK_IMAGE_PATH", length = 100)
    private String bookImagePath;

    @Column(name = "BOOK_ISBN", length = 30)
    private String bookIsbn;

    @Column(name = "BOOK_QUANTITY")
    private Integer bookQuantity;

    @Column(name = "UPDATED_BY")
    private Integer updatedBy;

    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

}
