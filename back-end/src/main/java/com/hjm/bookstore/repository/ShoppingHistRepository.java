package com.hjm.bookstore.repository;

import com.hjm.bookstore.entity.ShoppingHist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史订单数据访问层
 */
@Repository
public interface ShoppingHistRepository extends JpaRepository<ShoppingHist, Long> {
    
    /**
     * 根据用户ID查询订单历史
     */
    List<ShoppingHist> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    /**
     * 根据用户ID分页查询订单历史
     */
    Page<ShoppingHist> findByUserId(Integer userId, Pageable pageable);
    
    /**
     * 根据用户ID和订单状态分页查询订单历史
     */
    Page<ShoppingHist> findByUserIdAndOrderStatus(Integer userId, Integer orderStatus, Pageable pageable);
    
    /**
     * 查询指定时间范围内的订单
     */
    List<ShoppingHist> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 获取每日销量统计
     */
    @Query("SELECT DATE(sh.createdAt) as date, SUM(sh.quantity) as totalSales " +
           "FROM ShoppingHist sh " +
           "WHERE sh.createdAt >= :startDate AND sh.orderStatus = 1 " +
           "GROUP BY DATE(sh.createdAt) " +
           "ORDER BY DATE(sh.createdAt)")
    List<Object[]> getDailySalesStatistics(@Param("startDate") LocalDateTime startDate);
    
    /**
     * 获取今日销量前N的书籍
     */
    @Query("SELECT sh.bookId, SUM(sh.quantity) as totalQuantity " +
           "FROM ShoppingHist sh " +
           "WHERE DATE(sh.createdAt) = CURRENT_DATE AND sh.orderStatus = 1 " +
           "GROUP BY sh.bookId " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> getTodayTopBooks();
}
