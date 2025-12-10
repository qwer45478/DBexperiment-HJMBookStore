package com.hjm.bookstore.repository;

import com.hjm.bookstore.entity.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 用户评分数据访问层
 */
@Repository
public interface UserScoreRepository extends JpaRepository<UserScore, UserScore.UserScoreId> {
    
    /**
     * 查找用户对某本书的评分
     */
    Optional<UserScore> findByUserIdAndBookId(Integer userId, Integer bookId);
    
    /**
     * 获取某本书的所有评分
     */
    List<UserScore> findByBookId(Integer bookId);
    
    /**
     * 获取某用户的所有评分
     */
    List<UserScore> findByUserId(Integer userId);
    
    /**
     * 计算某本书的平均评分
     */
    @Query("SELECT AVG(us.score) FROM UserScore us WHERE us.bookId = :bookId")
    Double getAverageRatingByBookId(@Param("bookId") Integer bookId);
    
    /**
     * 获取所有用户评分数据（用于推荐算法）
     */
    @Query("SELECT us.userId, us.bookId, us.score FROM UserScore us")
    List<Object[]> getAllUserScores();
}
