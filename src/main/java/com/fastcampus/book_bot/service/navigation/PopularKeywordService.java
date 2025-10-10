package com.fastcampus.book_bot.service.navigation;

import com.fastcampus.book_bot.dto.book.SearchDTO;
import com.fastcampus.book_bot.dto.keyword.KeywordDTO;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularKeywordService {

    private final RedisTemplate<String, String> redisTemplate;
    private final Komoran komoran;

    private static final String POPULAR_KEYWORD = "popular:keywords:";

    /**
     * 검색시 호출
     * 키워드 추출 후 Redis에 저장
     */
    public void recordKeyword(SearchDTO searchDTO) {
        List<String> keywords = extractKeywords(searchDTO.getKeyword());

        String redisKey = POPULAR_KEYWORD + LocalDate.now();

        keywords.forEach(keyword -> {
            redisTemplate.opsForZSet().incrementScore(redisKey, keyword, 1);
        });
    }

    /**
     * 인기 검색어 조회
     */
    public List<KeywordDTO> getPopularKeywords(int limit) {
        try {
            String redisKey = POPULAR_KEYWORD + LocalDate.now();
            Set<ZSetOperations.TypedTuple<String>> results =
                    redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, 0, limit - 1);

            if (results == null || results.isEmpty()) {
                log.debug("오늘의 인기 검색어가 없습니다.");
                return List.of();
            }

            return results.stream()
                    .map(tuple -> new KeywordDTO(
                            tuple.getValue(),
                            tuple.getScore().intValue()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("인기 검색어 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * 키워드 추출
     */
    private List<String> extractKeywords(String query) {

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        try {

            KomoranResult result = komoran.analyze(query);
            List<String> keywords = result.getNouns();

            return keywords.stream()
                    .filter(keyword -> keyword.length() >= 2)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("키워드 추출 중 오류 발생: {}", query, e);

            // 공백으로 분리
            return List.of(query.trim().split("\\s+"))
                    .stream()
                    .filter(word -> word.length() >= 2)
                    .collect(Collectors.toList());
        }
    }
}
