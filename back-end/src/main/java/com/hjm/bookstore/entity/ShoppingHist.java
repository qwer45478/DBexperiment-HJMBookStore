package com.hjm.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 历史订单实体类
 */
@Entity
@Table(name = "shopping_hist")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingHist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "book_id", nullable = false)
    private Integer bookId;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "actual_pay", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualPay;
    
    @Column(name = "address", length = 500)
    private String address;
    
    @Column(name = "order_status")
    private Integer orderStatus = 1; // 0-已取消，1-运送中，2-已完成
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
