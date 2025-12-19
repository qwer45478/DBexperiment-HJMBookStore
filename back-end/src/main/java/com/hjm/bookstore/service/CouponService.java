package com.hjm.bookstore.service;

import com.hjm.bookstore.dto.AdminCouponSearchRequest;
import com.hjm.bookstore.dto.CouponResponse;
import com.hjm.bookstore.dto.PageResponse;
import com.hjm.bookstore.entity.CouponType;
import com.hjm.bookstore.entity.UserCoupon;
import com.hjm.bookstore.repository.CouponTypeRepository;
import com.hjm.bookstore.repository.UserCouponRepository;
import com.hjm.bookstore.repository.UserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
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
        // 检查用户今天是否已经登录过（使用latest_log字段判断）
        boolean hasLoggedInToday = userInfoRepository.hasLoggedInToday(userId);
        if (hasLoggedInToday) {
            log.info("用户今天已经登录过，不能重复领取每日优惠券: userId={}", userId);
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
<<<<<<< HEAD
            
=======

>>>>>>> b6e1a3b77a48cd5161991af423a72f441c01ea6a
            log.info("优惠券使用成功: userId={}, couponId={}", userId, couponId);
            return true;
        } catch (Exception e) {
            log.error("使用优惠券失败: userId={}, couponId={}", userId, couponId, e);
            return false;
        }
    }
    
    /**
     * 管理员获取所有优惠券类型
     */
    public List<Map<String, Object>> getAllCouponTypes() {
        List<CouponType> couponTypes = couponTypeRepository.findAll();
        
        return couponTypes.stream()
                .map(ct -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("couponId", ct.getCouponId());
                    map.put("couponName", ct.getCouponName());
                    map.put("discountAmount", ct.getDiscountAmount());
                    map.put("minAmount", ct.getMinAmount());
                    map.put("validDays", ct.getValidDays());
                    map.put("createdAt", ct.getCreatedAt());
                    
                    // 统计该优惠券的发放数量
                    long issuedCount = userCouponRepository.countByCouponId(ct.getCouponId());
                    map.put("issuedCount", issuedCount);
                    
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 管理员获取优惠券类型（带分页）
     */
    public PageResponse<Map<String, Object>> searchAdminCoupons(AdminCouponSearchRequest request) {
        request.validate();
        
        // 构建查询条件
        Specification<CouponType> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            if (request.getCouponName() != null && !request.getCouponName().trim().isEmpty()) {
                predicates.add(cb.like(root.get("couponName"), "%" + request.getCouponName() + "%"));
            }
            
            if (request.getMinDiscountAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("discountAmount"), request.getMinDiscountAmount()));
            }
            
            if (request.getMaxDiscountAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("discountAmount"), request.getMaxDiscountAmount()));
            }
            
            if (request.getMinMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minAmount"), request.getMinMinAmount()));
            }
            
            if (request.getMaxMinAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("minAmount"), request.getMaxMinAmount()));
            }
            
            if (request.getValidDays() != null) {
                predicates.add(cb.equal(root.get("validDays"), request.getValidDays()));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        // 构建排序
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (request.getSortBy() != null) {
            Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortOrder()) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, request.getSortBy());
        }
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);
        
        // 执行查询
        Page<CouponType> page = couponTypeRepository.findAll(spec, pageable);
        
        // 转换为返回格式
        List<Map<String, Object>> content = page.getContent().stream()
                .map(ct -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("couponId", ct.getCouponId());
                    map.put("couponName", ct.getCouponName());
                    map.put("discountAmount", ct.getDiscountAmount());
                    map.put("minAmount", ct.getMinAmount());
                    map.put("validDays", ct.getValidDays());
                    map.put("createdAt", ct.getCreatedAt());
                    
                    // 统计该优惠券的发放数量
                    long issuedCount = userCouponRepository.countByCouponId(ct.getCouponId());
                    map.put("issuedCount", issuedCount);
                    
                    return map;
                })
                .collect(Collectors.toList());
        
        return PageResponse.of(content, request.getPage(), request.getSize(), page.getTotalElements());
    }
    
    /**
     * 管理员添加优惠券类型
     */
    @Transactional
    public String addCouponType(String couponName, Integer discountAmount, Integer minAmount, Integer validDays) {
        // 生成新的优惠券ID
        String maxId = couponTypeRepository.findMaxCouponId();
        String newId;
        if (maxId != null && !maxId.isEmpty()) {
            int nextId = Integer.parseInt(maxId) + 1;
            newId = String.format("%02d", nextId);
        } else {
            newId = "01";
        }
        
        CouponType couponType = new CouponType();
        couponType.setCouponId(newId);
        couponType.setCouponName(couponName);
        couponType.setDiscountAmount(discountAmount);
        couponType.setMinAmount(minAmount);
        couponType.setValidDays(validDays);
        
        couponTypeRepository.save(couponType);
        
        log.info("管理员添加优惠券类型成功: couponId={}, couponName={}", newId, couponName);
        return newId;
    }
    
    /**
     * 管理员删除优惠券类型
     */
    @Transactional
    public boolean deleteCouponType(String couponId) {
        try {
            // 检查是否有用户已领取该优惠券
            long issuedCount = userCouponRepository.countByCouponId(couponId);
            if (issuedCount > 0) {
                log.warn("优惠券已被领取，无法删除: couponId={}, issuedCount={}", couponId, issuedCount);
                return false;
            }
            
            couponTypeRepository.deleteById(couponId);
            
            log.info("管理员删除优惠券类型成功: couponId={}", couponId);
            return true;
        } catch (Exception e) {
            log.error("删除优惠券类型失败: couponId={}", couponId, e);
            return false;
        }
    }
    
    /**
     * 管理员为用户发放优惠券
     */
    @Transactional
    public int issueCouponsToUsers(String couponId, Integer quantity, String targetType, Integer targetValue) {
        // 检查优惠券是否存在
        Optional<CouponType> couponOpt = couponTypeRepository.findById(couponId);
        if (!couponOpt.isPresent()) {
            throw new RuntimeException("优惠券不存在: " + couponId);
        }
        
        List<Integer> targetUserIds = new ArrayList<>();
        
        if ("level".equals(targetType)) {
            // 按用户等级发放
            targetUserIds = userInfoRepository.findUserIdsByLevel(targetValue);
        } else if ("user".equals(targetType)) {
            // 按用户ID发放
            targetUserIds.add(targetValue);
        } else {
            throw new RuntimeException("无效的目标类型: " + targetType);
        }
        
        if (targetUserIds.isEmpty()) {
            throw new RuntimeException("没有找到目标用户");
        }
        
        int issuedCount = 0;
        
        for (Integer userId : targetUserIds) {
            for (int i = 0; i < quantity; i++) {
                try {
                    // 检查用户是否已拥有该优惠券
                    if (!userCouponRepository.existsById(userId, couponId)) {
                        UserCoupon userCoupon = new UserCoupon();
                        userCoupon.setId(new UserCoupon.UserCouponId(userId, couponId));
                        userCouponRepository.save(userCoupon);
                        issuedCount++;
                    }
                } catch (Exception e) {
                    log.warn("发放优惠券失败: userId={}, couponId={}", userId, couponId, e);
                }
            }
        }
        
        log.info("管理员发放优惠券成功: couponId={}, targetType={}, targetValue={}, issuedCount={}", 
                couponId, targetType, targetValue, issuedCount);
        
        return issuedCount;
    }
}
