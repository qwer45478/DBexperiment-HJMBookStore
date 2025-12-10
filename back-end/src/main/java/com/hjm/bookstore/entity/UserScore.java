package com.hjm.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户评分实体类
 */
@Entity
@Table(name = "user_score")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserScore.UserScoreId.class)
public class UserScore {
    
    @Id
    @Column(name = "user_id")
    private Integer userId;
    
    @Id
    @Column(name = "book_id")
    private Integer bookId;
    
    @Column(name = "score", nullable = false)
    private Integer score;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * 复合主键类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserScoreId implements Serializable {
        private Integer userId;
        private Integer bookId;
    }
}
