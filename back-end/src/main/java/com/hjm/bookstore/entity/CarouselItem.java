package com.hjm.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 首页轮播图实体类
 */
@Entity
@Table(name = "carousel_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarouselItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carousel_id")
    private Integer carouselId;
    
    @Column(name = "book_id", nullable = false)
    private Integer bookId;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
