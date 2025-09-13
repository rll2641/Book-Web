package com.fastcampus.book_bot.controller;

import com.fastcampus.book_bot.domain.book.Book;
import com.fastcampus.book_bot.service.order.BestSellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final BestSellerService bestSellerService;

    @GetMapping
    public String main(Model model) {
        try {
            // 월간 베스트셀러 데이터 조회 및 캐시 업데이트
            bestSellerService.getMonthBestSeller();
            List<Book> monthlyBestSellers = bestSellerService.getMonthBestSeller();

            // 주간 베스트셀러 데이터 조회 및 캐시 업데이트
            bestSellerService.getWeekBestSeller();
            List<Book> weeklyBestSellers = bestSellerService.getWeekBestSeller();

            // 모델에 데이터 추가
            model.addAttribute("monthlyBestSellers", monthlyBestSellers);
            model.addAttribute("weeklyBestSellers", weeklyBestSellers);

            log.info("BestSellers loaded - Monthly: {}, Weekly: {}",
                    monthlyBestSellers.size(), weeklyBestSellers.size());

        } catch (Exception e) {
            log.error("Error loading bestsellers", e);
            // 에러 발생시 빈 리스트로 처리
            model.addAttribute("monthlyBestSellers", List.of());
            model.addAttribute("weeklyBestSellers", List.of());
        }

        return "index";
    }
}
