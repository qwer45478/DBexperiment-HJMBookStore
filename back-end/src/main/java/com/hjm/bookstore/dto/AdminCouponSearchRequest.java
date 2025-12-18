package com.hjm.bookstore.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员优惠券搜索请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminCouponSearchRequest extends PageRequest {
    
    /**
     * 优惠券名称
     */
    private String couponName;
    
    /**
     * 最小优惠额度
     */
    private Integer minDiscountAmount;
    
    /**
     * 最大优惠额度
     */
    private Integer maxDiscountAmount;
    
    /**
     * 最小使用门槛
     */
    private Integer minMinAmount;
    
    /**
     * 最大使用门槛
     */
    private Integer maxMinAmount;
    
    /**
     * 有效期筛选
     */
    private Integer validDays;
    
    /**
     * 排序字段：couponName, discountAmount, minAmount, validDays, createdAt
     */
    private String sortBy;
    
    /**
     * 排序方向：asc, desc
     */
    private String sortOrder;
}
