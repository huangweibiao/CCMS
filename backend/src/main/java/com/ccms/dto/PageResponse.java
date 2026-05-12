package com.ccms.dto;

import java.util.List;
import java.util.Objects;

/**
 * 分页响应DTO
 */
public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    public PageResponse() {
    }

    public PageResponse(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static <T> PageResponseBuilder<T> builder() {
        return new PageResponseBuilder<T>();
    }

    /**
     * 从Spring Data Page对象创建PageResponse
     */
    public static <T> PageResponse<T> fromPage(org.springframework.data.domain.Page<T> page) {
        if (page == null) {
            return null;
        }
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageResponse<?> that = (PageResponse<?>) o;
        return pageNumber == that.pageNumber && 
               pageSize == that.pageSize && 
               totalElements == that.totalElements && 
               totalPages == that.totalPages && 
               Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, pageNumber, pageSize, totalElements, totalPages);
    }

    @Override
    public String toString() {
        return "PageResponse{" +
                "content=" + content +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                '}';
    }

    public static class PageResponseBuilder<T> {
        private List<T> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;

        PageResponseBuilder() {
        }

        public PageResponseBuilder<T> content(List<T> content) {
            this.content = content;
            return this;
        }

        public PageResponseBuilder<T> pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public PageResponseBuilder<T> pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PageResponseBuilder<T> totalElements(long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public PageResponseBuilder<T> totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public PageResponse<T> build() {
            return new PageResponse<T>(content, pageNumber, pageSize, totalElements, totalPages);
        }

        @Override
        public String toString() {
            return "PageResponse.PageResponseBuilder(content=" + this.content + ", pageNumber=" + this.pageNumber + ", pageSize=" + this.pageSize + ", totalElements=" + this.totalElements + ", totalPages=" + this.totalPages + ")";
        }
    }
}