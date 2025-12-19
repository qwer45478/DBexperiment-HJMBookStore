package com.hjm.bookstore.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单搜索请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderSearchRequest extends PageRequest {
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 订单状态：0-已取消，1-运送中，2-已完成
     */
    private Integer orderStatus;
    
    /**
     * 排序字段：createTime, updateTime
     */
    private String sortBy;
    
    /**
     * 排序方向：asc, desc
     */
    private String sortOrder;
}
