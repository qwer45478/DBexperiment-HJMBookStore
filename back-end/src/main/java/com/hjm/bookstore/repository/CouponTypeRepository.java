package com.hjm.bookstore.repository;

import com.hjm.bookstore.entity.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 优惠券类型数据访问接口
 */
@Repository
public interface CouponTypeRepository extends JpaRepository<CouponType, String> {
    
    /**
     * 根据优惠券ID查找优惠券
     */
    Optional<CouponType> findByCouponId(String couponId);
    
    /**
     * 根据优惠券名称查找优惠券
     */
    List<CouponType> findByCouponNameContaining(String couponName);
    
    /**
     * 查找所有可用的优惠券
     */
    @Query("SELECT c FROM CouponType c WHERE c.discountAmount > 0 ORDER BY c.discountAmount DESC")
    List<CouponType> findAllAvailableCoupons();
    
    /**
     * 根据优惠额度范围查找优惠券
     */
    @Query("SELECT c FROM CouponType c WHERE c.discountAmount >= :minAmount AND c.discountAmount <= :maxAmount ORDER BY c.discountAmount DESC")
    List<CouponType> findByDiscountAmountBetween(@Param("minAmount") Integer minAmount, @Param("maxAmount") Integer maxAmount);
}
