package com.hjm.bookstore.dto;

import lombok.Data;

/**
 * 分页请求基础类
 */
@Data
public class PageRequest {
    
    /**
     * 页码，从1开始
     */
    private Integer page = 1;
    
    /**
     * 每页大小，默认10
     */
    private Integer size = 10;
    
    /**
     * 获取偏移量
     */
    public Integer getOffset() {
        return (page - 1) * size;
    }
    
    /**
     * 验证分页参数
     */
    public void validate() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        // 限制最大每页数量
        if (size > 100) {
            size = 100;
        }
    }
}
