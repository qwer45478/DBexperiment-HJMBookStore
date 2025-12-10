package com.hjm.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户地址实体类
 */
@Entity
@Table(name = "user_add")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "address", nullable = false, length = 500)
    private String address;
    
    @Column(name = "is_default")
    private Integer isDefault = 0;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
