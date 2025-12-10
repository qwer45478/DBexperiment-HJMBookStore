package com.hjm.bookstore.repository;

import com.hjm.bookstore.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 购物车数据访问层
 */
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {
    
    /**
     * 根据用户ID查询购物车
     */
    List<ShoppingCart> findByUserId(Integer userId);
    
    /**
     * 查找用户购物车中的特定书籍
     */
    Optional<ShoppingCart> findByUserIdAndBookId(Integer userId, Integer bookId);
    
    /**
     * 删除用户购物车中的特定书籍
     */
    void deleteByUserIdAndBookId(Integer userId, Integer bookId);
    
    /**
     * 清空用户购物车
     */
    void deleteByUserId(Integer userId);
}
