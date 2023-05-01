package com.example.codebase.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageInfo {

    private int page;

    private int size;

    private int totalPages;

    private long totalElements;

    public static PageInfo of(int page, int size, int totalPages, long totalElements) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(page);
        pageInfo.setSize(size);
        pageInfo.setTotalPages(totalPages);
        pageInfo.setTotalElements(totalElements);
        return pageInfo;
    }
}
