package com.hjm.bookstore.controller;

import com.hjm.bookstore.common.Result;
import com.hjm.bookstore.dto.AdminCouponSearchRequest;
import com.hjm.bookstore.dto.CouponResponse;
import com.hjm.bookstore.dto.PageResponse;
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
    
    /**
     * 管理员获取所有优惠券类型
     */
    @GetMapping("/admin/types")
    public Result<List<Map<String, Object>>> getAllCouponTypes() {
        try {
            List<Map<String, Object>> couponTypes = couponService.getAllCouponTypes();
            return Result.success(couponTypes);
        } catch (Exception e) {
            log.error("获取优惠券类型失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员获取优惠券类型（带分页）
     */
    @PostMapping("/admin/search")
    public Result<PageResponse<Map<String, Object>>> getAdminCouponTypesWithPagination(@RequestBody AdminCouponSearchRequest request) {
        try {
            PageResponse<Map<String, Object>> result = couponService.searchAdminCoupons(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取管理员优惠券列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 管理员添加优惠券类型
     */
    @PostMapping("/admin/types")
    public Result<String> addCouponType(@RequestBody Map<String, Object> request) {
        try {
            String couponName = (String) request.get("couponName");
            Integer discountAmount = (Integer) request.get("discountAmount");
            Integer minAmount = (Integer) request.get("minAmount");
            Integer validDays = (Integer) request.get("validDays");
            
            String couponId = couponService.addCouponType(couponName, discountAmount, minAmount, validDays);
            return Result.success("优惠券添加成功，ID: " + couponId);
        } catch (Exception e) {
            log.error("添加优惠券类型失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 管理员删除优惠券类型
     */
    @DeleteMapping("/admin/types/{couponId}")
    public Result<String> deleteCouponType(@PathVariable String couponId) {
        try {
            boolean success = couponService.deleteCouponType(couponId);
            if (success) {
                return Result.success("优惠券删除成功");
            } else {
                return Result.error("优惠券删除失败，可能已被使用");
            }
        } catch (Exception e) {
            log.error("删除优惠券类型失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 管理员为用户发放优惠券
     */
    @PostMapping("/admin/issue")
    public Result<String> issueCouponsToUsers(@RequestBody Map<String, Object> request) {
        try {
            String couponId = (String) request.get("couponId");
            Integer quantity = (Integer) request.get("quantity");
            String targetType = (String) request.get("targetType"); // "level" 或 "user"
            Integer targetValue = (Integer) request.get("targetValue"); // 用户等级或用户ID
            
            int issuedCount = couponService.issueCouponsToUsers(couponId, quantity, targetType, targetValue);
            return Result.success("成功发放 " + issuedCount + " 张优惠券");
        } catch (Exception e) {
            log.error("发放优惠券失败", e);
            return Result.error(e.getMessage());
        }
    }
}
