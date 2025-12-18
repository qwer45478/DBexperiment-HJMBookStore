package com.hjm.bookstore.repository;

import com.hjm.bookstore.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * 用户信息数据访问层
 */
@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    
    /**
     * 根据电话号码查找用户
     */
    Optional<UserInfo> findByPhone(String phone);
    
    /**
     * 检查电话号码是否存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 更新用户最新登录时间
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserInfo u SET u.latestLog = CURRENT_TIMESTAMP WHERE u.userId = :userId")
    void updateLatestLog(@Param("userId") Integer userId);
    
    /**
     * 检查用户今天是否已经登录过
     */
    @Query("SELECT COUNT(u) > 0 FROM UserInfo u WHERE u.userId = :userId AND DATE(u.latestLog) = CURRENT_DATE")
    boolean hasLoggedInToday(@Param("userId") Integer userId);
    
    /**
     * 根据用户等级查找用户ID列表
     */
    @Query("SELECT u.userId FROM UserInfo u WHERE u.userLevel = :userLevel")
    List<Integer> findUserIdsByLevel(@Param("userLevel") Integer userLevel);
}
