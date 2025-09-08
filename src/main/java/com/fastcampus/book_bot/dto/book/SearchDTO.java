package com.fastcampus.book_bot.dto.book;

import com.fastcampus.book_bot.domain.book.Book;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Data
public class SearchDTO {

    /* 요청 필드 */
    private String keyword;
    private String searchType = "all";

    /* 응답 필드 */
    private Page<Book> searchResult;
    private Integer pageSize;
    private String sortProperty;
    private String sortDirection;

    public void setPageInfo(Pageable pageable) {
        this.pageSize = pageable.getPageSize();
        this.sortProperty = getSortProperty(pageable);
        this.sortDirection = getSortDirection(pageable);
    }

    private String getSortProperty(Pageable pageable) {
        return pageable.getSort().iterator().hasNext() ?
                pageable.getSort().iterator().next().getProperty() : "bookPubdate";
    }

    private String getSortDirection(Pageable pageable) {
        return pageable.getSort().iterator().hasNext() ?
                pageable.getSort().iterator().next().getDirection().name() : "DESC";
    }


}