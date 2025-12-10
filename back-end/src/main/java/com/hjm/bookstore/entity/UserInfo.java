package com.hjm.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户信息实体类
 */
@Entity
@Table(name = "user_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "username", nullable = false, length = 48)
    private String username;
    
    @Column(name = "phone", nullable = false, unique = true, length = 11)
    private String phone;
    
    @Column(name = "password", nullable = false, length = 64)
    private String password;
    
    @Column(name = "user_level")
    private Integer userLevel = 0;
    
    @Column(name = "total_spending", precision = 10, scale = 2)
    private BigDecimal totalSpending = BigDecimal.ZERO;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 根据消费金额更新用户等级
     */
    public void updateUserLevel() {
        if (totalSpending.compareTo(new BigDecimal("1998")) >= 0) {
            userLevel = 5;
        } else if (totalSpending.compareTo(new BigDecimal("648")) >= 0) {
            userLevel = 4;
        } else if (totalSpending.compareTo(new BigDecimal("328")) >= 0) {
            userLevel = 3;
        } else if (totalSpending.compareTo(new BigDecimal("198")) >= 0) {
            userLevel = 2;
        } else if (totalSpending.compareTo(new BigDecimal("30")) >= 0) {
            userLevel = 1;
        } else {
            userLevel = 0;
        }
    }
}
