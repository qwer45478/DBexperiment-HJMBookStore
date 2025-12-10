package com.hjm.bookstore.controller;

import com.hjm.bookstore.common.Result;
import com.hjm.bookstore.entity.UserAddress;
import com.hjm.bookstore.entity.UserInfo;
import com.hjm.bookstore.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public Result<UserInfo> getUserInfo(@PathVariable Integer userId) {
        try {
            return userService.getUserInfo(userId)
                    .map(Result::success)
                    .orElse(Result.error("用户不存在"));
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<UserInfo> updateUserInfo(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            String username = (String) request.get("username");
            String phone = (String) request.get("phone");

            UserInfo user = userService.updateUserInfo(userId, username, phone);
            return Result.success("更新成功", user);
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户地址列表
     */
    @GetMapping("/{userId}/addresses")
    public Result<List<UserAddress>> getUserAddresses(@PathVariable Integer userId) {
        try {
            List<UserAddress> addresses = userService.getUserAddresses(userId);
            return Result.success(addresses);
        } catch (Exception e) {
            log.error("获取地址列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加用户地址
     */
    @PostMapping("/addresses")
    public Result<UserAddress> addAddress(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            String address = (String) request.get("address");
            Integer isDefault = (Integer) request.getOrDefault("isDefault", 0);

            UserAddress userAddress = userService.addAddress(userId, address, isDefault);
            return Result.success("添加成功", userAddress);
        } catch (Exception e) {
            log.error("添加地址失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户地址
     */
    @PutMapping("/addresses")
    public Result<UserAddress> updateAddress(@RequestBody Map<String, Object> request) {
        try {
            Integer addressId = (Integer) request.get("addressId");
            String address = (String) request.get("address");
            Integer isDefault = (Integer) request.get("isDefault");

            UserAddress userAddress = userService.updateAddress(addressId, address, isDefault);
            return Result.success("更新成功", userAddress);
        } catch (Exception e) {
            log.error("更新地址失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户地址
     */
    @DeleteMapping("/addresses/{addressId}")
    public Result<Void> deleteAddress(@PathVariable Integer addressId) {
        try {
            userService.deleteAddress(addressId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除地址失败", e);
            return Result.error(e.getMessage());
        }
    }
}
