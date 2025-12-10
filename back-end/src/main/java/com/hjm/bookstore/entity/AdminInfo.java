package com.hjm.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 管理员信息实体类
 */
@Entity
@Table(name = "admin_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminInfo {
    
    @Id
    @Column(name = "admin_id", length = 8)
    private String adminId;
    
    @Column(name = "admin_password", nullable = false, length = 64)
    private String adminPassword;
    
    @Column(name = "admin_level", nullable = false)
    private Integer adminLevel;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
