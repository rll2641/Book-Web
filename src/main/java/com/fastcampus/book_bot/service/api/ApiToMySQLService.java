package com.fastcampus.book_bot.service.api;

import com.fastcampus.book_bot.domain.Book;
import com.fastcampus.book_bot.dto.api.BookDTO;
import com.fastcampus.book_bot.dto.api.NaverBookResponseDTO;
import com.fastcampus.book_bot.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ApiToMySQLService {

    /* 네이버 API를 통해 도서 정보를 검색하고 MySQL에 저장하는 서비스
     * - 도서 정보를 검색하고, 중복된 도서는 저장하지 않음
     * - 응답이 없거나 오류가 발생하면 로그에 기록
     * - 저장된 도서의 개수를 로그에 출력
     * - 트랜잭션을 사용하여 데이터 일관성 유지
    * */


    private final BookRepository bookRepository;
    private final NaverBookAPIService naverBookAPIService;

    public ApiToMySQLService(BookRepository bookRepository, NaverBookAPIService naverBookAPIService) {
        this.bookRepository = bookRepository;
        this.naverBookAPIService = naverBookAPIService;
    }

    public List<Book> searchAndSaveBooks(String query, int start, int display) {
        NaverBookResponseDTO response = naverBookAPIService.searchBooks(query, start, display);

        if (response.getItems() == null || response.getItems().length == 0) {
            log.warn("조건에 맞는 도서가 없습니다. 검색어: {}", query);
            return Collections.emptyList();
        }

        return saveBooks(response);
    }

    @Transactional
    public List<Book> saveBooks(NaverBookResponseDTO response) {
        List<Book> savedBooks = new ArrayList<>();

        for (BookDTO item : response.getItems()) {
            try {
                Book book = convertToBook(item);

                if (!isDuplicateBook(book)) {
                    Book savedBook = bookRepository.save(book);
                    savedBooks.add(savedBook);
                }
            } catch (Exception e) {
                {
                    log.error("도서 저장 중 오류 발생: {}, 도서: {}", e.getMessage(), item.getTitle());
                    return Collections.emptyList();
                }
            }
        }

        return savedBooks;
    }

    private Book convertToBook(BookDTO item) {
        return Book.builder()
                .bookTitle(item.getTitle())
                .bookAuthor(item.getAuthor())
                .bookLink(item.getLink())
                .bookImage(item.getImage())
                .bookPublisher(item.getPublisher())
                .bookIsbn(item.getIsbn())
                .bookDescription(item.getDescription())
                .bookPubdate(item.getPubdate())
                .bookDiscount(item.getDiscount())
                .build();
    }

    private boolean isDuplicateBook(Book book) {

        if (book.getBookIsbn() != null && book.getBookIsbn() != 0) {
            Optional<Book> existingBook = bookRepository.findByBookIsbn(book.getBookIsbn());
            return existingBook.isPresent();
        }
        return false;
    }
}
