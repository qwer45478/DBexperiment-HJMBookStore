package com.hjm.bookstore.repository;

import com.hjm.bookstore.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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
}
