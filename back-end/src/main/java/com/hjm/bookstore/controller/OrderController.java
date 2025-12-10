package com.hjm.bookstore.controller;

import com.hjm.bookstore.common.Result;
import com.hjm.bookstore.entity.ShoppingHist;
import com.hjm.bookstore.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<Object> createOrder(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            
            // 判断是单个订单还是批量订单
            if (request.containsKey("items")) {
                // 批量订单（购物车结算）
                @SuppressWarnings("unchecked")
                List<Map<String, Integer>> items = (List<Map<String, Integer>>) request.get("items");
                List<ShoppingHist> orders = orderService.createBatchOrders(userId, items);
                return Result.success("订单创建成功", orders);
            } else {
                // 单个订单
                Integer bookId = (Integer) request.get("bookId");
                Integer quantity = (Integer) request.get("quantity");
                ShoppingHist order = orderService.createOrder(userId, bookId, quantity);
                return Result.success("订单创建成功", order);
            }
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户订单历史
     */
    @GetMapping("/{userId}")
    public Result<List<Map<String, Object>>> getUserOrders(@PathVariable Integer userId) {
        try {
            List<Map<String, Object>> orders = orderService.getUserOrders(userId);
            return Result.success(orders);
        } catch (Exception e) {
            log.error("获取订单历史失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取订单统计（管理员）
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = orderService.getOrderStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }
}
