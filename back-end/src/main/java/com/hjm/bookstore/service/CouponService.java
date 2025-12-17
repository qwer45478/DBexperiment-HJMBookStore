package com.hjm.bookstore.service;

import com.hjm.bookstore.dto.CouponResponse;
import com.hjm.bookstore.entity.CouponType;
import com.hjm.bookstore.entity.UserCoupon;
import com.hjm.bookstore.repository.CouponTypeRepository;
import com.hjm.bookstore.repository.UserCouponRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 优惠券服务类
 */
@Service
@Slf4j
public class CouponService {
    
    @Autowired
    private CouponTypeRepository couponTypeRepository;
    
    @Autowired
    private UserCouponRepository userCouponRepository;
    
    /**
     * 获取用户所有优惠券
     */
    public List<CouponResponse> getUserCoupons(Integer userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);
        
        return userCoupons.stream()
                .map(uc -> {
                    Optional<CouponType> couponTypeOpt = couponTypeRepository.findById(uc.getId().getCouponId());
                    if (couponTypeOpt.isPresent()) {
                        CouponType ct = couponTypeOpt.get();
                        return new CouponResponse(
                                ct.getCouponId(),
                                ct.getCouponName(),
                                ct.getDiscountAmount(),
                                ct.getMinAmount(),
                                ct.getValidDays(),
                                uc.getObtainedAt()
                        );
                    }
                    return null;
                })
                .filter(coupon -> coupon != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 领取优惠券
     */
    @Transactional
    public boolean claimCoupon(Integer userId, String couponId) {
        try {
            // 检查优惠券是否存在
            Optional<CouponType> couponOpt = couponTypeRepository.findById(couponId);
            if (!couponOpt.isPresent()) {
                log.warn("优惠券不存在: {}", couponId);
                return false;
            }
            
            // 检查是否已经领取过
            Optional<UserCoupon> existingCoupon = userCouponRepository.findById(userId, couponId);
            if (existingCoupon.isPresent()) {
                log.warn("用户已经领取过该优惠券: userId={}, couponId={}", userId, couponId);
                return false;
            }
            
            // 创建用户优惠券记录
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setId(new UserCoupon.UserCouponId(userId, couponId));
            userCouponRepository.save(userCoupon);
            
            log.info("用户成功领取优惠券: userId={}, couponId={}", userId, couponId);
            return true;
        } catch (Exception e) {
            log.error("领取优惠券失败: userId={}, couponId={}", userId, couponId, e);
            return false;
        }
    }
    
    /**
     * 5级用户每日领取优惠券
     */
    @Transactional
    public boolean claimDailyCoupon(Integer userId) {
        // 检查今天是否已经领取过
        boolean hasClaimedToday = userCouponRepository.hasClaimedCouponToday(userId, "01");
        if (hasClaimedToday) {
            log.info("用户今天已经领取过每日优惠券: userId={}", userId);
            return false;
        }
        
        return claimCoupon(userId, "01");
    }
    
    /**
     * 获取用户可用优惠券（按优惠额度排序）
     */
    public List<CouponResponse> getUserAvailableCoupons(Integer userId, BigDecimal orderAmount) {
        List<CouponResponse> allCoupons = getUserCoupons(userId);
        
        return allCoupons.stream()
                .filter(coupon -> !coupon.getIsExpired() && coupon.getIsUsable())
                .filter(coupon -> orderAmount.compareTo(BigDecimal.valueOf(coupon.getMinAmount())) >= 0)
                .sorted((c1, c2) -> c2.getDiscountAmount().compareTo(c1.getDiscountAmount()))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取即将过期的优惠券
     */
    public List<CouponResponse> getExpiringCoupons(Integer userId) {
        LocalDateTime sevenDaysLater = LocalDateTime.now().plusDays(7);
        List<UserCoupon> expiringCoupons = userCouponRepository.findExpiringCoupons(userId, sevenDaysLater);
        
        return expiringCoupons.stream()
                .map(uc -> {
                    Optional<CouponType> couponTypeOpt = couponTypeRepository.findById(uc.getId().getCouponId());
                    if (couponTypeOpt.isPresent()) {
                        CouponType ct = couponTypeOpt.get();
                        return new CouponResponse(
                                ct.getCouponId(),
                                ct.getCouponName(),
                                ct.getDiscountAmount(),
                                ct.getMinAmount(),
                                ct.getValidDays(),
                                uc.getObtainedAt()
                        );
                    }
                    return null;
                })
                .filter(coupon -> coupon != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 计算优惠券折扣金额
     */
    public BigDecimal calculateCouponDiscount(String couponId, BigDecimal orderAmount) {
        if (couponId == null || couponId.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        Optional<CouponType> couponOpt = couponTypeRepository.findById(couponId);
        if (!couponOpt.isPresent()) {
            return BigDecimal.ZERO;
        }
        
        CouponType coupon = couponOpt.get();
        
        // 检查是否满足使用条件
        if (orderAmount.compareTo(BigDecimal.valueOf(coupon.getMinAmount())) < 0) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(coupon.getDiscountAmount());
    }
    
    /**
     * 使用优惠券（删除用户优惠券记录）
     */
    @Transactional
    public boolean useCoupon(Integer userId, String couponId) {
        try {
            if (couponId == null || couponId.trim().isEmpty()) {
                return false;
            }
            
            // 检查用户是否拥有该优惠券
            Optional<UserCoupon> userCouponOpt = userCouponRepository.findById(userId, couponId);
            if (!userCouponOpt.isPresent()) {
                log.warn("用户没有该优惠券: userId={}, couponId={}", userId, couponId);
                return false;
            }
            
            // 删除用户优惠券记录
            userCouponRepository.deleteById(new UserCoupon.UserCouponId(userId, couponId));
            
            log.info("优惠券使用成功: userId={}, couponId={}", userId, couponId);
            return true;
        } catch (Exception e) {
            log.error("使用优惠券失败: userId={}, couponId={}", userId, couponId, e);
            return false;
        }
    }
}
