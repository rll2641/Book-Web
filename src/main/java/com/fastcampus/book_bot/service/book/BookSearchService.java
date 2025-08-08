package com.fastcampus.book_bot.service.book;

import com.fastcampus.book_bot.domain.Book;
import com.fastcampus.book_bot.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class BookSearchService {

    private final BookRepository bookRepository;

    public BookSearchService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String keyword, String searchType, Pageable pageable) {

        try {
            switch (searchType) {
                case "title":
                    return bookRepository.findByBookTitleContaining(keyword, pageable);
                case "author":
                    return bookRepository.findByBookAuthorContaining(keyword, pageable);
                case "publisher":
                    return bookRepository.findByBookPublisherContaining(keyword, pageable);
                case "all":
                default:
                    return bookRepository.findByBookTitleContainingOrBookAuthorContainingOrBookPublisherContaining(keyword, keyword, keyword, pageable);
            }
        } catch (Exception e) {
            log.warn("도서 검색 중 오류 발생! 조건: {}, 키워드 {}", searchType, keyword);
            return Page.empty(pageable);
        }
    }
}
