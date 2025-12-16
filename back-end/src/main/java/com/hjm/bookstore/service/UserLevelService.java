package com.hjm.bookstore.service;

import com.hjm.bookstore.entity.UserInfo;
import com.hjm.bookstore.repository.UserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户等级服务类
 */
@Service
@Slf4j
public class UserLevelService {
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
    // 用户等级折扣配置
    private static final Map<Integer, BigDecimal> DISCOUNT_RATES = new HashMap<>();
    // 用户等级名称配置
    private static final Map<Integer, String> LEVEL_NAMES = new HashMap<>();
    // 运费配置
    private static final BigDecimal SHIPPING_FEE = BigDecimal.valueOf(2.0);
    
    static {
        // 初始化折扣率：0级-原价，1级-98折，2级-97折，3级-95折，4级-95折，5级-95折
        DISCOUNT_RATES.put(0, BigDecimal.valueOf(1.0));
        DISCOUNT_RATES.put(1, BigDecimal.valueOf(0.98));
        DISCOUNT_RATES.put(2, BigDecimal.valueOf(0.97));
        DISCOUNT_RATES.put(3, BigDecimal.valueOf(0.95));
        DISCOUNT_RATES.put(4, BigDecimal.valueOf(0.95));
        DISCOUNT_RATES.put(5, BigDecimal.valueOf(0.95));
        
        // 初始化等级名称
        LEVEL_NAMES.put(0, "普通会员");
        LEVEL_NAMES.put(1, "铜牌会员");
        LEVEL_NAMES.put(2, "银牌会员");
        LEVEL_NAMES.put(3, "金牌会员");
        LEVEL_NAMES.put(4, "白金会员");
        LEVEL_NAMES.put(5, "钻石会员");
    }
    
    /**
     * 获取用户折扣率
     */
    public BigDecimal getDiscountRate(Integer userLevel) {
        return DISCOUNT_RATES.getOrDefault(userLevel, BigDecimal.valueOf(1.0));
    }
    
    /**
     * 获取用户等级名称
     */
    public String getLevelName(Integer userLevel) {
        return LEVEL_NAMES.getOrDefault(userLevel, "普通会员");
    }
    
    /**
     * 检查用户是否免运费
     */
    public boolean isFreeShipping(Integer userLevel) {
        return userLevel >= 4; // 4级和5级用户免运费
    }
    
    /**
     * 计算订单折扣金额
     */
    public BigDecimal calculateDiscount(Integer userLevel, BigDecimal originalPrice) {
        BigDecimal discountRate = getDiscountRate(userLevel);
        BigDecimal discountAmount = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate));
        return discountAmount;
    }
    
    /**
     * 计算运费
     */
    public BigDecimal calculateShippingFee(Integer userLevel) {
        return isFreeShipping(userLevel) ? BigDecimal.ZERO : SHIPPING_FEE;
    }
    
    /**
     * 计算最终结算金额
     * 公式：最终金额 = 原价 * 折扣率 + 运费 - 优惠券额度
     */
    public BigDecimal calculateFinalAmount(Integer userLevel, BigDecimal originalPrice, BigDecimal couponDiscount) {
        BigDecimal discountRate = getDiscountRate(userLevel);
        BigDecimal discountedPrice = originalPrice.multiply(discountRate);
        BigDecimal shippingFee = calculateShippingFee(userLevel);
        BigDecimal finalAmount = discountedPrice.add(shippingFee).subtract(couponDiscount);
        
        // 确保最终金额不为负数
        return finalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmount;
    }
    
    /**
     * 获取用户等级详情
     */
    public Map<String, Object> getUserLevelInfo(Integer userId) {
        UserInfo user = userInfoRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        
        Map<String, Object> levelInfo = new HashMap<>();
        levelInfo.put("userLevel", user.getUserLevel());
        levelInfo.put("levelName", getLevelName(user.getUserLevel()));
        levelInfo.put("discountRate", getDiscountRate(user.getUserLevel()));
        levelInfo.put("freeShipping", isFreeShipping(user.getUserLevel()));
        levelInfo.put("totalSpending", user.getTotalSpending());
        levelInfo.put("nextLevelSpending", getNextLevelSpending(user.getUserLevel()));
        
        return levelInfo;
    }
    
    /**
     * 获取下一等级所需消费金额
     */
    public BigDecimal getNextLevelSpending(Integer currentLevel) {
        switch (currentLevel) {
            case 0: return BigDecimal.valueOf(30);
            case 1: return BigDecimal.valueOf(198);
            case 2: return BigDecimal.valueOf(328);
            case 3: return BigDecimal.valueOf(648);
            case 4: return BigDecimal.valueOf(1998);
            case 5: return BigDecimal.valueOf(1998); // 已是最高等级
            default: return BigDecimal.ZERO;
        }
    }
    
    /**
     * 更新用户等级（基于消费金额）
     */
    public void updateUserLevel(Integer userId) {
        UserInfo user = userInfoRepository.findById(userId).orElse(null);
        if (user != null) {
            user.updateUserLevel();
            userInfoRepository.save(user);
            log.info("用户等级已更新: userId={}, newLevel={}", userId, user.getUserLevel());
        }
    }
    
    /**
     * 获取所有会员等级说明
     */
    public Map<Integer, Map<String, Object>> getAllLevelDescriptions() {
        Map<Integer, Map<String, Object>> descriptions = new HashMap<>();
        
        for (int level = 0; level <= 5; level++) {
            Map<String, Object> levelDesc = new HashMap<>();
            levelDesc.put("levelName", LEVEL_NAMES.get(level));
            levelDesc.put("discountRate", DISCOUNT_RATES.get(level));
            levelDesc.put("discountPercentage", BigDecimal.ONE.subtract(DISCOUNT_RATES.get(level)).multiply(BigDecimal.valueOf(100)));
            levelDesc.put("freeShipping", isFreeShipping(level));
            levelDesc.put("dailyCoupon", level == 5); // 只有5级用户有每日优惠券
            levelDesc.put("requiredSpending", getNextLevelSpending(level - 1));
            
            descriptions.put(level, levelDesc);
        }
        
        return descriptions;
    }
}
