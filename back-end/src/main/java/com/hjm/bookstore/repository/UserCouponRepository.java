package com.hjm.bookstore.repository;

import com.hjm.bookstore.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户优惠券数据访问接口
 */
@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, UserCoupon.UserCouponId> {
    
    /**
     * 根据用户ID查找所有优惠券
     */
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.id.userId = :userId ORDER BY uc.obtainedAt DESC")
    List<UserCoupon> findByUserId(@Param("userId") Integer userId);
    
    /**
     * 根据用户ID和优惠券ID查找（嵌入式主键字段）
     */
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.id.userId = :userId AND uc.id.couponId = :couponId")
    Optional<UserCoupon> findByUserIdAndCouponId(@Param("userId") Integer userId,
                                                 @Param("couponId") String couponId);
    
    /**
     * 查找用户指定优惠券ID的优惠券（使用复合主键）
     */
    default Optional<UserCoupon> findById(Integer userId, String couponId) {
        return findByUserIdAndCouponId(userId, couponId);
    }
    
    /**
     * 查找用户即将过期的优惠券（7天内过期）
     */
    @Query("SELECT uc FROM UserCoupon uc JOIN CouponType ct ON uc.id.couponId = ct.couponId " +
           "WHERE uc.id.userId = :userId AND ct.validDays > 0 " +
           "AND uc.obtainedAt < :expiryDate ORDER BY uc.obtainedAt ASC")
    List<UserCoupon> findExpiringCoupons(@Param("userId") Integer userId, @Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * 检查用户今天是否已经领取过指定优惠券
     */
    @Query("SELECT COUNT(uc) > 0 FROM UserCoupon uc WHERE uc.id.userId = :userId " +
           "AND uc.id.couponId = :couponId AND DATE(uc.obtainedAt) = CURRENT_DATE")
    boolean hasClaimedCouponToday(@Param("userId") Integer userId, @Param("couponId") String couponId);
    
    /**
     * 统计用户拥有的优惠券数量
     */
    @Query("SELECT COUNT(uc) FROM UserCoupon uc WHERE uc.id.userId = :userId")
    Long countByUserId(@Param("userId") Integer userId);
    
    /**
     * 统计指定优惠券的发放数量
     */
    @Query("SELECT COUNT(uc) FROM UserCoupon uc WHERE uc.id.couponId = :couponId")
    Long countByCouponId(@Param("couponId") String couponId);
    
    /**
     * 检查用户是否拥有指定优惠券
     */
    @Query("SELECT COUNT(uc) > 0 FROM UserCoupon uc WHERE uc.id.userId = :userId AND uc.id.couponId = :couponId")
    boolean existsById(@Param("userId") Integer userId, @Param("couponId") String couponId);
}
