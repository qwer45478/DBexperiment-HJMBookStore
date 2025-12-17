package com.hjm.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体类
 */
@Entity
@Table(name = "user_coup")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {
    
    @EmbeddedId
    private UserCouponId id;
    
    @Column(name = "obtained_at", updatable = false)
    private LocalDateTime obtainedAt;
    
    @PrePersist
    protected void onCreate() {
        obtainedAt = LocalDateTime.now();
    }
    
    /**
     * 复合主键类
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCouponId {
        
        @Column(name = "user_id")
        private Integer userId;
        
        @Column(name = "coupon_id", length = 10)
        private String couponId;
    }
}
