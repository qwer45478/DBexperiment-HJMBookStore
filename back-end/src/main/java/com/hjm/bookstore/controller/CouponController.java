package com.hjm.bookstore.controller;

import com.hjm.bookstore.common.Result;
import com.hjm.bookstore.dto.CouponResponse;
import com.hjm.bookstore.service.CouponService;
import com.hjm.bookstore.service.UserLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 优惠券控制器
 */
@RestController
@RequestMapping("/coupons")
@Slf4j
public class CouponController {
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private UserLevelService userLevelService;
    
    /**
     * 获取用户所有优惠券
     */
    @GetMapping("/user/{userId}")
    public Result<List<CouponResponse>> getUserCoupons(@PathVariable Integer userId) {
        try {
            List<CouponResponse> coupons = couponService.getUserCoupons(userId);
            return Result.success(coupons);
        } catch (Exception e) {
            log.error("获取用户优惠券失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 领取优惠券
     */
    @PostMapping("/claim")
    public Result<String> claimCoupon(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            String couponId = (String) request.get("couponId");
            
            boolean success = couponService.claimCoupon(userId, couponId);
            if (success) {
                return Result.success("优惠券领取成功");
            } else {
                return Result.error("优惠券领取失败，可能已经领取过或优惠券不存在");
            }
        } catch (Exception e) {
            log.error("领取优惠券失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 5级用户每日领取优惠券
     */
    @PostMapping("/daily")
    public Result<String> claimDailyCoupon(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            
            // 检查用户等级
            Map<String, Object> levelInfo = userLevelService.getUserLevelInfo(userId);
            if (levelInfo == null) {
                return Result.error("用户不存在");
            }
            
            Integer userLevel = (Integer) levelInfo.get("userLevel");
            if (userLevel < 5) {
                return Result.error("只有钻石会员才能领取每日优惠券");
            }
            
            boolean success = couponService.claimDailyCoupon(userId);
            if (success) {
                return Result.success("每日优惠券领取成功");
            } else {
                return Result.error("今日已经领取过每日优惠券");
            }
        } catch (Exception e) {
            log.error("领取每日优惠券失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户可用优惠券（用于订单结算）
     */
    @GetMapping("/available/{userId}")
    public Result<List<CouponResponse>> getAvailableCoupons(
            @PathVariable Integer userId,
            @RequestParam(required = false) BigDecimal orderAmount) {
        try {
            List<CouponResponse> coupons;
            if (orderAmount != null) {
                coupons = couponService.getUserAvailableCoupons(userId, orderAmount);
            } else {
                coupons = couponService.getUserCoupons(userId);
            }
            return Result.success(coupons);
        } catch (Exception e) {
            log.error("获取可用优惠券失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取即将过期的优惠券
     */
    @GetMapping("/expiring/{userId}")
    public Result<List<CouponResponse>> getExpiringCoupons(@PathVariable Integer userId) {
        try {
            List<CouponResponse> coupons = couponService.getExpiringCoupons(userId);
            return Result.success(coupons);
        } catch (Exception e) {
            log.error("获取即将过期优惠券失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 计算优惠券折扣金额
     */
    @GetMapping("/calculate-discount")
    public Result<BigDecimal> calculateDiscount(
            @RequestParam String couponId,
            @RequestParam BigDecimal orderAmount) {
        try {
            BigDecimal discount = couponService.calculateCouponDiscount(couponId, orderAmount);
            return Result.success(discount);
        } catch (Exception e) {
            log.error("计算优惠券折扣失败", e);
            return Result.error(e.getMessage());
        }
    }
}
