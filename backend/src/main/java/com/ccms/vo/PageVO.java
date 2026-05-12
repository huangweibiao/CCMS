package com.ccms.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页数据封装类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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