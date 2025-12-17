package com.hjm.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确认响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmResponse {
    
    private List<OrderItemResponse> items;
    private String address;
    private BigDecimal originalPrice;      // 原价
    private BigDecimal shippingFee;        // 运费
    private BigDecimal discountRate;       // 折扣率
    private BigDecimal discountAmount;     // 折扣金额
    private BigDecimal couponDiscount;     // 优惠券折扣
    private BigDecimal finalPrice;         // 最终价格
    private String userLevelName;          // 用户等级名称
    private List<CouponResponse> availableCoupons; // 可用优惠券列表
    
    /**
     * 订单项响应
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Integer bookId;
        private String bookName;
        private String author;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
