package com.hjm.bookstore.repository;

import com.hjm.bookstore.entity.CarouselItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 首页轮播图数据访问层
 */
@Repository
public interface CarouselItemRepository extends JpaRepository<CarouselItem, Integer> {
    
    /**
     * 按排序顺序获取所有轮播图
     */
    List<CarouselItem> findAllByOrderBySortOrderAsc();
    
    /**
     * 根据书籍ID删除轮播图
     */
    void deleteByBookId(Integer bookId);
}
