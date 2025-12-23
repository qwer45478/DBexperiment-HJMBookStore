package com.hjm.bookstore.repository;

import com.hjm.bookstore.entity.BooksInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 书籍信息数据访问层
 */
@Repository
public interface BooksInfoRepository extends JpaRepository<BooksInfo, Integer>, JpaSpecificationExecutor<BooksInfo> {
    
    /**
     * 根据书名模糊查询
     */
    List<BooksInfo> findByBookNameContainingAndStatus(String bookName, Integer status);
    
    /**
     * 根据作者模糊查询
     */
    List<BooksInfo> findByAuthorContainingAndStatus(String author, Integer status);
    
    /**
     * 根据分类查询
     */
    List<BooksInfo> findByCategoryAndStatus(String category, Integer status);
    
    /**
     * 获取销量排行榜
     */
    List<BooksInfo> findTop10ByStatusOrderBySalesDesc(Integer status);
    
    /**
     * 获取月销量排行榜
     */
    List<BooksInfo> findTop10ByStatusOrderByMonthlySalesDesc(Integer status);
    
    /**
     * 获取评分排行榜
     */
    List<BooksInfo> findTop10ByStatusOrderByRatingDesc(Integer status);

    /**
     * 原子扣减库存
     * @param bookId 书籍ID
     * @param quantity 扣减数量
     * @return 受影响行数
     */
    @Modifying
    @Query("UPDATE BooksInfo b SET b.stock = b.stock - :quantity, b.sales = b.sales + :quantity, b.monthlySales = b.monthlySales + :quantity WHERE b.bookId = :bookId AND b.stock >= :quantity")
    int decreaseStock(Integer bookId, Integer quantity);

    /**
     * 原子恢复库存
     * @param bookId 书籍ID
     * @param quantity 恢复数量
     * @return 受影响行数
     */
    @Modifying
    @Query("UPDATE BooksInfo b SET b.stock = b.stock + :quantity, b.sales = CASE WHEN b.sales >= :quantity THEN b.sales - :quantity ELSE 0 END, b.monthlySales = CASE WHEN b.monthlySales >= :quantity THEN b.monthlySales - :quantity ELSE 0 END WHERE b.bookId = :bookId")
    int increaseStock(Integer bookId, Integer quantity);
    
    /**
     * 获取所有上架书籍
     */
    List<BooksInfo> findByStatus(Integer status);
    
    /**
     * 根据书名、作者和出版社查找书籍（用于检查重复）
     */
    Optional<BooksInfo> findByBookNameAndAuthorAndPublisher(String bookName, String author, String publisher);
}
