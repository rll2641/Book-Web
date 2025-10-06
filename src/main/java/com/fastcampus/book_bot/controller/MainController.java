package com.fastcampus.book_bot.controller;

import com.fastcampus.book_bot.common.utils.JwtUtil;
import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.dto.keyword.KeywordDTO;
import com.fastcampus.book_bot.service.navigation.PopularKeywordService;
import com.fastcampus.book_bot.service.navigation.RecentlyViewService;
import com.fastcampus.book_bot.service.order.BestSellerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final BestSellerService bestSellerService;
    private final PopularKeywordService popularKeywordService;
    private final RecentlyViewService recentlyViewService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public String main(HttpServletRequest request,
                       Model model) {
        try {
            // 월간 베스트셀러 데이터 조회 및 캐시 업데이트
            bestSellerService.getMonthBestSeller();
            List<Book> monthlyBestSellers = bestSellerService.getMonthBestSeller();

            // 주간 베스트셀러 데이터 조회 및 캐시 업데이트
            bestSellerService.getWeekBestSeller();
            List<Book> weeklyBestSellers = bestSellerService.getWeekBestSeller();

            List<KeywordDTO> popularKeywords = popularKeywordService.getPopularKeywords(5);

            model.addAttribute("monthlyBestSellers", monthlyBestSellers);
            model.addAttribute("weeklyBestSellers", weeklyBestSellers);
            model.addAttribute("popularKeywords", popularKeywords);

            log.info("BestSellers loaded - Monthly: {}, Weekly: {}",
                    monthlyBestSellers.size(), weeklyBestSellers.size());

        } catch (Exception e) {
            log.error("Error loading bestsellers", e);
            model.addAttribute("monthlyBestSellers", List.of());
            model.addAttribute("weeklyBestSellers", List.of());
        }

        return "index";
    }

    @GetMapping("/test/db")
    public String mainWithDB(Model model) {
        try {
            List<Book> monthlyBestSellers = bestSellerService.getMonthBestSellerFromDB();
            List<Book> weeklyBestSellers = bestSellerService.getWeekBestSellerFromDB();

            model.addAttribute("monthlyBestSellers", monthlyBestSellers);
            model.addAttribute("weeklyBestSellers", weeklyBestSellers);

            log.info("DB BestSellers loaded - Monthly: {}, Weekly: {}",
                    monthlyBestSellers.size(), weeklyBestSellers.size());
        } catch (Exception e) {
            log.error("Error loading bestsellers from DB", e);
            model.addAttribute("monthlyBestSellers", List.of());
            model.addAttribute("weeklyBestSellers", List.of());
        }
        return "index";
    }
}
