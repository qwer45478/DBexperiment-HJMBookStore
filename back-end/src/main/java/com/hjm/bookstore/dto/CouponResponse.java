package com.hjm.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 优惠券响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {
    
    private String couponId;
    private String couponName;
    private Integer discountAmount;
    private Integer minAmount;
    private Integer validDays;
    private LocalDateTime obtainedAt;
    private LocalDateTime expiryDate;
    private Boolean isExpired;
    private Boolean isUsable;
    
    /**
     * 构造函数，用于从实体类创建响应对象
     */
    public CouponResponse(String couponId, String couponName, Integer discountAmount, 
                         Integer minAmount, Integer validDays, LocalDateTime obtainedAt) {
        this.couponId = couponId;
        this.couponName = couponName;
        this.discountAmount = discountAmount;
        this.minAmount = minAmount;
        this.validDays = validDays;
        this.obtainedAt = obtainedAt;
        
        // 计算过期时间
        if (validDays != null && validDays > 0 && obtainedAt != null) {
            this.expiryDate = obtainedAt.plusDays(validDays);
            this.isExpired = LocalDateTime.now().isAfter(this.expiryDate);
        } else {
            this.expiryDate = null;
            this.isExpired = false;
        }
        
        this.isUsable = !this.isExpired;
    }
}
