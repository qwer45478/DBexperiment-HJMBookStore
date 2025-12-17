package com.hjm.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 优惠券类型实体类
 */
@Entity
@Table(name = "coupon_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponType {
    
    @Id
    @Column(name = "coupon_id", length = 10)
    private String couponId;
    
    @Column(name = "coupon_name", nullable = false, length = 16)
    private String couponName;
    
    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;
    
    @Column(name = "min_amount", nullable = false)
    private Integer minAmount = 0;
    
    @Column(name = "valid_days", nullable = false)
    private Integer validDays = 0;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
