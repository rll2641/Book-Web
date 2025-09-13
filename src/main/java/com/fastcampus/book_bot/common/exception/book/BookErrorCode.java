package com.fastcampus.book_bot.common.exception.book;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookErrorCode {

    // ============== 도서 조회 관련 ==============
    NOT_FOUND("BOOK_NOT_FOUND", "도서를 찾을 수 없습니다"),
    ISBN_NOT_FOUND("BOOK_ISBN_NOT_FOUND", "해당 ISBN의 도서를 찾을 수 없습니다"),
    TITLE_NOT_FOUND("BOOK_TITLE_NOT_FOUND", "해당 제목의 도서를 찾을 수 없습니다"),
    AUTHOR_NOT_FOUND("BOOK_AUTHOR_NOT_FOUND", "해당 저자의 도서를 찾을 수 없습니다"),

    // ============== 도서 데이터 검증 관련 ==============
    INVALID_DATA("BOOK_INVALID_DATA", "유효하지 않은 도서 데이터입니다"),
    INVALID_ISBN_FORMAT("BOOK_INVALID_ISBN_FORMAT", "올바르지 않은 ISBN 형식입니다"),
    INVALID_TITLE_FORMAT("BOOK_INVALID_TITLE_FORMAT", "올바르지 않은 도서 제목 형식입니다"),
    INVALID_AUTHOR_FORMAT("BOOK_INVALID_AUTHOR_FORMAT", "올바르지 않은 저자명 형식입니다"),
    INVALID_PUBLISHER_FORMAT("BOOK_INVALID_PUBLISHER_FORMAT", "올바르지 않은 출판사명 형식입니다"),
    INVALID_PUBLICATION_DATE("BOOK_INVALID_PUBLICATION_DATE", "올바르지 않은 출간일입니다"),
    INVALID_PAGE_COUNT("BOOK_INVALID_PAGE_COUNT", "올바르지 않은 페이지 수입니다"),
    INVALID_PRICE("BOOK_INVALID_PRICE", "올바르지 않은 가격입니다"),

    // ============== 중복 관련 ==============
    ISBN_ALREADY_EXISTS("BOOK_ISBN_ALREADY_EXISTS", "이미 존재하는 ISBN입니다"),
    DUPLICATE_BOOK("BOOK_DUPLICATE_BOOK", "이미 등록된 도서입니다"),

    // ============== 도서 상태 관련 ==============
    OUT_OF_STOCK("BOOK_OUT_OF_STOCK", "품절된 도서입니다"),
    DISCONTINUED("BOOK_DISCONTINUED", "절판된 도서입니다"),
    NOT_AVAILABLE("BOOK_NOT_AVAILABLE", "현재 이용할 수 없는 도서입니다"),
    RESTRICTED_ACCESS("BOOK_RESTRICTED_ACCESS", "접근이 제한된 도서입니다"),

    // ============== 도서 검색 관련 ==============
    SEARCH_FAILED("BOOK_SEARCH_FAILED", "도서 검색에 실패했습니다"),
    INVALID_SEARCH_KEYWORD("BOOK_INVALID_SEARCH_KEYWORD", "올바르지 않은 검색어입니다"),
    NO_SEARCH_RESULTS("BOOK_NO_SEARCH_RESULTS", "검색 결과가 없습니다"),
    SEARCH_TIMEOUT("BOOK_SEARCH_TIMEOUT", "검색 요청 시간이 초과되었습니다"),

    // ============== 도서 리뷰/평점 관련 ==============
    REVIEW_NOT_FOUND("BOOK_REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다"),
    INVALID_RATING("BOOK_INVALID_RATING", "올바르지 않은 평점입니다"),
    REVIEW_ALREADY_EXISTS("BOOK_REVIEW_ALREADY_EXISTS", "이미 리뷰를 작성했습니다"),
    REVIEW_ACCESS_DENIED("BOOK_REVIEW_ACCESS_DENIED", "리뷰 접근 권한이 없습니다"),

    // ============== 도서 카테고리 관련 ==============
    CATEGORY_NOT_FOUND("BOOK_CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다"),
    INVALID_CATEGORY("BOOK_INVALID_CATEGORY", "올바르지 않은 카테고리입니다"),

    // ============== 외부 API 관련 ==============
    EXTERNAL_API_ERROR("BOOK_EXTERNAL_API_ERROR", "외부 도서 API 오류가 발생했습니다"),
    API_RATE_LIMIT_EXCEEDED("BOOK_API_RATE_LIMIT_EXCEEDED", "API 요청 한도를 초과했습니다"),
    API_UNAVAILABLE("BOOK_API_UNAVAILABLE", "외부 도서 서비스를 이용할 수 없습니다"),

    // ============== 시스템 에러 ==============
    SYSTEM_ERROR("BOOK_SYSTEM_ERROR", "시스템 오류가 발생했습니다"),
    DATABASE_ERROR("BOOK_DATABASE_ERROR", "데이터베이스 오류가 발생했습니다"),
    FILE_UPLOAD_ERROR("BOOK_FILE_UPLOAD_ERROR", "파일 업로드 오류가 발생했습니다"),
    IMAGE_PROCESSING_ERROR("BOOK_IMAGE_PROCESSING_ERROR", "이미지 처리 오류가 발생했습니다");

    private final String code;
    private final String message;
}