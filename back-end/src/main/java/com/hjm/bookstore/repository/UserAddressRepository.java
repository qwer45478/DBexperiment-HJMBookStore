package com.hjm.bookstore.repository;

import com.hjm.bookstore.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 用户地址数据访问层
 */
@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Integer> {
    
    /**
     * 根据用户ID查询所有地址
     */
    List<UserAddress> findByUserId(Integer userId);
    
    /**
     * 查找用户的默认地址
     */
    Optional<UserAddress> findByUserIdAndIsDefault(Integer userId, Integer isDefault);
}
