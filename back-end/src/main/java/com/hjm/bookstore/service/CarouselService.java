package com.hjm.bookstore.service;

import com.hjm.bookstore.entity.BooksInfo;
import com.hjm.bookstore.entity.CarouselItem;
import com.hjm.bookstore.repository.BooksInfoRepository;
import com.hjm.bookstore.repository.CarouselItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 轮播图服务类
 */
@Service
@Slf4j
public class CarouselService {

    @Autowired
    private CarouselItemRepository carouselItemRepository;

    @Autowired
    private BooksInfoRepository booksInfoRepository;

    /**
     * 获取轮播图列表（包含书籍信息）
     */
    public List<Map<String, Object>> getCarouselList() {
        List<CarouselItem> items = carouselItemRepository.findAllByOrderBySortOrderAsc();
        List<Map<String, Object>> result = new ArrayList<>();

        for (CarouselItem item : items) {
            Optional<BooksInfo> bookOpt = booksInfoRepository.findById(item.getBookId());
            if (bookOpt.isPresent()) {
                BooksInfo book = bookOpt.get();
                Map<String, Object> carouselInfo = new HashMap<>();
                carouselInfo.put("carouselId", item.getCarouselId());
                carouselInfo.put("bookId", book.getBookId());
                carouselInfo.put("bookName", book.getBookName());
                carouselInfo.put("author", book.getAuthor());
                carouselInfo.put("bookImage", book.getBookImage());
                carouselInfo.put("sortOrder", item.getSortOrder());
                result.add(carouselInfo);
            }
        }

        return result;
    }

    /**
     * 添加轮播图
     */
    @Transactional
    public CarouselItem addCarouselItem(Integer bookId, Integer sortOrder) {
        // 检查书籍是否存在
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            throw new RuntimeException("书籍不存在");
        }

        CarouselItem item = new CarouselItem();
        item.setBookId(bookId);
        item.setSortOrder(sortOrder != null ? sortOrder : 0);
        
        CarouselItem saved = carouselItemRepository.save(item);
        log.info("添加轮播图: 书籍ID={}", bookId);
        return saved;
    }

    /**
     * 删除轮播图
     */
    @Transactional
    public void removeCarouselItem(Integer carouselId) {
        carouselItemRepository.deleteById(carouselId);
        log.info("删除轮播图: ID={}", carouselId);
    }
}
