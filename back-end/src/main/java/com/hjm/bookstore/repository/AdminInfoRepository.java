package com.hjm.bookstore.repository;

import com.hjm.bookstore.entity.AdminInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 管理员信息数据访问层
 */
@Repository
public interface AdminInfoRepository extends JpaRepository<AdminInfo, String> {
    
    /**
     * 根据管理员ID查找
     */
    Optional<AdminInfo> findByAdminId(String adminId);
    
    /**
     * 检查管理员ID是否存在
     */
    boolean existsByAdminId(String adminId);
}
