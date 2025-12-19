package com.hjm.bookstore.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.List;

/**
 * 书籍搜索请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BookSearchRequest extends PageRequest {
    
    private String bookName;
    private String author;
    private List<String> categories;
    private Integer minSales;
    private Integer maxSales;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private BigDecimal maxRating;
    private String sortBy; // "sales", "rating", "price"
    private String sortOrder; // "asc", "desc"
}
