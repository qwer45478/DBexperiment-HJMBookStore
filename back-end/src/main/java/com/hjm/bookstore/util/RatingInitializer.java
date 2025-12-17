package com.hjm.bookstore.util;

import com.hjm.bookstore.repository.BooksInfoRepository;
import com.hjm.bookstore.repository.UserScoreRepository;
import com.hjm.bookstore.entity.BooksInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 应用启动时初始化评分数据
 * 用于同步 user_score 表和 books_info 表的评分数据
 */
@Component
@Slf4j
public class RatingInitializer implements CommandLineRunner {

    @Autowired
    private BooksInfoRepository booksInfoRepository;

    @Autowired
    private UserScoreRepository userScoreRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("开始同步书籍评分数据...");

        try {
            // 获取所有有评分的书籍ID
            List<Integer> bookIdsWithScores = userScoreRepository.findAll()
                .stream()
                .map(score -> score.getBookId())
                .distinct()
                .toList();

            log.info("发现 {} 本书籍有评分数据", bookIdsWithScores.size());

            int updatedCount = 0;
            for (Integer bookId : bookIdsWithScores) {
                // 计算平均评分
                Double avgRating = userScoreRepository.getAverageRatingByBookId(bookId);

                if (avgRating != null) {
                    BooksInfo book = booksInfoRepository.findById(bookId).orElse(null);
                    if (book != null) {
                        BigDecimal oldRating = book.getRating();
                        BigDecimal newRating = BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP);

                        // 只在评分不同时更新
                        if (oldRating == null || oldRating.compareTo(newRating) != 0) {
                            book.setRating(newRating);
                            booksInfoRepository.save(book);
                            updatedCount++;
                            log.debug("更新书籍 {} (ID:{}) 的评分: {} -> {}",
                                book.getBookName(), bookId, oldRating, newRating);
                        }
                    }
                }
            }

            log.info("评分同步完成，共更新 {} 本书籍的评分", updatedCount);
        } catch (Exception e) {
            log.error("同步评分数据失败", e);
        }
    }
}

