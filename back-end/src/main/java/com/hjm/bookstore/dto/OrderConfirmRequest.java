package com.hjm.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确认请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmRequest {
    
    private Integer userId;
    private List<OrderItemRequest> items;
    private String address;
    private String couponId;
    
    /**
     * 订单项请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private Integer bookId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
