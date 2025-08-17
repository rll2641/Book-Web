package com.fastcampus.book_bot.service.api;

import com.fastcampus.book_bot.common.response.ApiResponse;
import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.dto.api.BookDTO;
import com.fastcampus.book_bot.dto.api.NaverBookResponseDTO;
import com.fastcampus.book_bot.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

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

    @Transactional
    public ApiResponse<NaverBookResponseDTO> searchAndSaveBooks(String query, int start, int display) {
        NaverBookResponseDTO response = naverBookAPIService.searchBooks(query, start, display);

        if (response.getItems() == null || response.getItems().length == 0) {
            log.warn("조건에 맞는 도서가 없습니다. 검색어: {}", query);
            return ApiResponse.error("검색 결과가 없습니다.");
        }

        saveBooks(response);

        return ApiResponse.success(response, "도서 저장 완료");
    }

    @Transactional
    protected void saveBooks(NaverBookResponseDTO response) {
        Random random = new Random();

        for (BookDTO item : response.getItems()) {
            try {
                Book book = convertToBook(item);
                book.setBookQuantity(30 + random.nextInt(21));
                if (!isDuplicateBook(book)) {
                    bookRepository.save(book);
                }
            } catch (Exception e) {
                {
                    log.error("도서 저장 중 오류 발생: {}, 도서: {}", e.getMessage(), item.getTitle());
                }
            }
        }
    }

    private Book convertToBook(BookDTO item) {
        return Book.builder()
                .bookName(item.getTitle())
                .bookAuthor(item.getAuthor())
                .bookLink(item.getLink())
                .bookImagePath(item.getImage())
                .bookPublisher(item.getPublisher())
                .bookIsbn(String.valueOf(item.getIsbn()))
                .bookDescription(item.getDescription())
                .bookPubdate(item.getPubdate())
                .bookDiscount(item.getDiscount())
                .build();
    }

    private boolean isDuplicateBook(Book book) {
        String isbn = book.getBookIsbn();

        if (isbn == null || isbn.trim().isEmpty() || isbn.equals("0")) {
            return false;
        }

        Optional<Book> existingBook = bookRepository.findByBookIsbn(isbn);
        return existingBook.isPresent();
    }
}
