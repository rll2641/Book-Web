package com.fastcampus.book_bot.controller.book;

import com.fastcampus.book_bot.domain.Book;
import com.fastcampus.book_bot.dto.book.SearchDTO;
import com.fastcampus.book_bot.service.book.BookSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@Slf4j
public class BookController {

    private final BookSearchService bookSearchService;

    public BookController(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }

    @GetMapping("/search")
    public String searchBooks(@ModelAttribute SearchDTO searchDTO,
                              @PageableDefault(size = 10, sort = "bookPubdate", direction = Sort.Direction.DESC) Pageable pageable,
                              Model model) {

        Page<Book> searchResult = bookSearchService.searchBooks(
                searchDTO.getKeyword(),
                searchDTO.getSearchType(),
                pageable
        );

        searchDTO.setSearchResult(searchResult);
        searchDTO.setPageInfo(pageable);

        model.addAttribute("search", searchDTO);

        return "book/search";
    }
}
