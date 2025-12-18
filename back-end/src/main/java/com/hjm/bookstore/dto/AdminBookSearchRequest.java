package com.hjm.bookstore.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员书籍搜索请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminBookSearchRequest extends PageRequest {
    
    /**
     * 书名
     */
    private String bookName;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 出版社
     */
    private String publisher;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 最小价格
     */
    private java.math.BigDecimal minPrice;
    
    /**
     * 最大价格
     */
    private java.math.BigDecimal maxPrice;
    
    /**
     * 书籍状态：1-上架，0-下架
     */
    private Integer status;
    
    /**
     * 排序字段：bookName, author, price, stock, sales, rating, createdAt
     */
    private String sortBy;
    
    /**
     * 排序方向：asc, desc
     */
    private String sortOrder;
}
