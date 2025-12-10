package com.hjm.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 书籍信息实体类
 */
@Entity
@Table(name = "books_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BooksInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer bookId;
    
    @Column(name = "book_name", nullable = false, length = 200)
    private String bookName;
    
    @Column(name = "category", nullable = false, length = 1)
    private String category;
    
    @Column(name = "author", nullable = false, length = 100)
    private String author;
    
    @Column(name = "book_image", length = 500)
    private String bookImage;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "publisher", nullable = false, length = 100)
    private String publisher;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "rating", precision = 3, scale = 1)
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "stock")
    private Integer stock = 0;
    
    @Column(name = "sales")
    private Integer sales = 0;
    
    @Column(name = "monthly_sales")
    private Integer monthlySales = 0;
    
    @Column(name = "status")
    private Integer status = 1;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
