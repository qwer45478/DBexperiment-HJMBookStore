package com.hjm.bookstore.controller;

import com.hjm.bookstore.common.Result;
import com.hjm.bookstore.entity.ShoppingCart;
import com.hjm.bookstore.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 获取用户购物车
     */
    @GetMapping("/{userId}")
    public Result<List<Map<String, Object>>> getCartItems(@PathVariable Integer userId) {
        try {
            List<Map<String, Object>> items = shoppingCartService.getCartItems(userId);
            return Result.success(items);
        } catch (Exception e) {
            log.error("获取购物车失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加到购物车
     */
    @PostMapping("/add")
    public Result<ShoppingCart> addToCart(@RequestBody Map<String, Integer> request) {
        try {
            Integer userId = request.get("userId");
            Integer bookId = request.get("bookId");
            Integer quantity = request.getOrDefault("quantity", 1);
            
            ShoppingCart cart = shoppingCartService.addToCart(userId, bookId, quantity);
            return Result.success("添加成功", cart);
        } catch (Exception e) {
            log.error("添加到购物车失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新购物车数量
     */
    @PutMapping("/update")
    public Result<ShoppingCart> updateQuantity(@RequestBody Map<String, Integer> request) {
        try {
            Integer cartId = request.get("cartId");
            Integer quantity = request.get("quantity");
            
            ShoppingCart cart = shoppingCartService.updateQuantity(cartId, quantity);
            return Result.success("更新成功", cart);
        } catch (Exception e) {
            log.error("更新购物车失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除购物车项
     */
    @DeleteMapping("/remove/{cartId}")
    public Result<Void> removeCartItem(@PathVariable Integer cartId) {
        try {
            shoppingCartService.removeCartItem(cartId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除购物车项失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear/{userId}")
    public Result<Void> clearCart(@PathVariable Integer userId) {
        try {
            shoppingCartService.clearCart(userId);
            return Result.success("清空成功", null);
        } catch (Exception e) {
            log.error("清空购物车失败", e);
            return Result.error(e.getMessage());
        }
    }
}
