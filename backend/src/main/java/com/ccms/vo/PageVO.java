package com.ccms.vo;

import java.util.List;
import java.util.Objects;

/**
 * 分页数据封装类
 */
public class PageVO<T> {
    
    /**
     * 数据内容
     */
    private List<T> content;
    
    /**
     * 总记录数
     */
    private Long totalElements;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 当前页码
     */
    private Integer number;
    
    /**
     * 是否有前一页
     */
    private boolean hasPrevious;
    
    /**
     * 是否有下一页
     */
    private boolean hasNext;
    
    /**
     * 是否是第一页
     */
    private boolean first;
    
    /**
     * 是否是最后一页
     */
    private boolean last;
    
    // 无参构造函数
    public PageVO() {}
    
    // 全参构造函数
    public PageVO(List<T> content, Long totalElements, Integer totalPages, Integer size, 
                 Integer number, boolean hasPrevious, boolean hasNext, boolean first, boolean last) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.size = size;
        this.number = number;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
        this.first = first;
        this.last = last;
    }
    
    // Getter方法
    public List<T> getContent() {
        return content;
    }
    
    public Long getTotalElements() {
        return totalElements;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public Integer getNumber() {
        return number;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public boolean isFirst() {
        return first;
    }
    
    public boolean isLast() {
        return last;
    }
    
    // Setter方法
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public void setNumber(Integer number) {
        this.number = number;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public void setFirst(boolean first) {
        this.first = first;
    }
    
    public void setLast(boolean last) {
        this.last = last;
    }
    
    // equals方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageVO<?> pageVO = (PageVO<?>) o;
        return hasPrevious == pageVO.hasPrevious &&
                hasNext == pageVO.hasNext &&
                first == pageVO.first &&
                last == pageVO.last &&
                Objects.equals(content, pageVO.content) &&
                Objects.equals(totalElements, pageVO.totalElements) &&
                Objects.equals(totalPages, pageVO.totalPages) &&
                Objects.equals(size, pageVO.size) &&
                Objects.equals(number, pageVO.number);
    }
    
    // hashCode方法
    @Override
    public int hashCode() {
        return Objects.hash(content, totalElements, totalPages, size, number, 
                          hasPrevious, hasNext, first, last);
    }
    
    // toString方法
    @Override
    public String toString() {
        return "PageVO{" +
                "content=" + content +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", size=" + size +
                ", number=" + number +
                ", hasPrevious=" + hasPrevious +
                ", hasNext=" + hasNext +
                ", first=" + first +
                ", last=" + last +
                '}';
    }
    
    /**
     * 手动构建器方法（Lombok可能未正常工作）
     */
    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }
    
    /**
     * 手动构建器类
     */
    public static class Builder<T> {
        private List<T> content;
        private Long totalElements;
        private Integer totalPages;
        private Integer size;
        private Integer number;
        private boolean hasPrevious;
        private boolean hasNext;
        private boolean first;
        private boolean last;
        
        public Builder<T> content(List<T> content) {
            this.content = content;
            return this;
        }
        
        public Builder<T> totalElements(Long totalElements) {
            this.totalElements = totalElements;
            return this;
        }
        
        public Builder<T> totalPages(Integer totalPages) {
            this.totalPages = totalPages;
            return this;
        }
        
        public Builder<T> size(Integer size) {
            this.size = size;
            return this;
        }
        
        public Builder<T> number(Integer number) {
            this.number = number;
            return this;
        }
        
        public Builder<T> hasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
            return this;
        }
        
        public Builder<T> hasNext(boolean hasNext) {
            this.hasNext = hasNext;
            return this;
        }
        
        public Builder<T> first(boolean first) {
            this.first = first;
            return this;
        }
        
        public Builder<T> last(boolean last) {
            this.last = last;
            return this;
        }
        
        public PageVO<T> build() {
            PageVO<T> pageVO = new PageVO<>();
            pageVO.content = this.content;
            pageVO.totalElements = this.totalElements;
            pageVO.totalPages = this.totalPages;
            pageVO.size = this.size;
            pageVO.number = this.number;
            pageVO.hasPrevious = this.hasPrevious;
            pageVO.hasNext = this.hasNext;
            pageVO.first = this.first;
            pageVO.last = this.last;
            return pageVO;
        }
    }
    
    /**
     * 构建分页VO
     */
    public static <T> PageVO<T> of(List<T> content, Long totalElements, Integer totalPages, 
                                 Integer size, Integer number) {
        return PageVO.<T>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .size(size)
                .number(number)
                .hasPrevious(number > 0)
                .hasNext(number < totalPages - 1)
                .first(number == 0)
                .last(number >= totalPages - 1)
                .build();
    }
}