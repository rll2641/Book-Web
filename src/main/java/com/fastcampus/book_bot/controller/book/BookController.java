package com.fastcampus.book_bot.controller.book;

import com.fastcampus.book_bot.common.utils.JwtUtil;
import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.domain.user.User;
import com.fastcampus.book_bot.dto.book.SearchDTO;
import com.fastcampus.book_bot.service.book.BookSearchService;
import com.fastcampus.book_bot.service.navigation.PopularKeywordService;
import com.fastcampus.book_bot.service.navigation.RecentlyViewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookSearchService bookSearchService;
    private final PopularKeywordService popularKeywordService;
    private final RecentlyViewService recentlyViewService;
    private final JwtUtil jwtUtil;


    @GetMapping("/search")
    public String searchBooks(@ModelAttribute SearchDTO searchDTO,
                              @PageableDefault(size = 10, sort = "bookPubdate", direction = Sort.Direction.DESC) Pageable pageable,
                              Model model) {

        Page<Book> searchResult = bookSearchService.searchBooks(
                searchDTO.getKeyword(),
                searchDTO.getSearchType(),
                pageable
        );

        popularKeywordService.recordKeyword(searchDTO);

        searchDTO.setSearchResult(searchResult);
        searchDTO.setPageInfo(pageable);

        model.addAttribute("search", searchDTO);

        return "book/search";
    }

    @GetMapping("/book/{bookId}")
    public String bookDetail(@PathVariable Integer bookId,
                             HttpServletRequest request,
                             Model model) {

        Optional<Book> book = bookSearchService.getBookById(bookId);
        if (book.isEmpty()) {
            return "error/404";
        }

        model.addAttribute("book", book.get());

        Integer userId = jwtUtil.extractUserIdFromRequest(request);
        if (userId != null) {
            try {
                recentlyViewService.addRecentBook(userId, bookId);
                log.info("최근 본 상품 추가 - UserId: {}, BookId: {}", userId, bookId);
            } catch (Exception e) {
                log.error("최근 본 상품 추가 실패 - UserId: {}, BookId: {}", userId, bookId, e);
            }
        }

        return "book/detail";
    }

    @GetMapping("/api/recent-books")
    @ResponseBody
    public ResponseEntity<List<Book>> getRecentBooks(HttpServletRequest request) {
        Integer userId = jwtUtil.extractUserIdFromRequest(request);

        if (userId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        try {
            List<Book> recentBooks = recentlyViewService.getRecentBookIds(userId);
            return ResponseEntity.ok(recentBooks);
        } catch (Exception e) {
            log.error("최근 본 상품 조회 실패 - UserId: {}", userId, e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}
