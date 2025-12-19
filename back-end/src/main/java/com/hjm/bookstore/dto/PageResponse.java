package com.hjm.bookstore.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页响应类
 */
@Data
public class PageResponse<T> {
    
    /**
     * 数据列表
     */
    private List<T> content;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNext;
    
    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;
    
    /**
     * 是否为第一页
     */
    private Boolean isFirst;
    
    /**
     * 是否为最后一页
     */
    private Boolean isLast;
    
    public PageResponse() {}
    
    public PageResponse(List<T> content, Integer page, Integer size, Long total) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
        this.hasNext = page < totalPages;
        this.hasPrevious = page > 1;
        this.isFirst = page.equals(1);
        this.isLast = page.equals(totalPages);
    }
    
    /**
     * 静态工厂方法
     */
    public static <T> PageResponse<T> of(List<T> content, Integer page, Integer size, Long total) {
        return new PageResponse<>(content, page, size, total);
    }
}
