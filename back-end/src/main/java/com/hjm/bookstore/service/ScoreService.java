package com.hjm.bookstore.service;

import com.hjm.bookstore.entity.BooksInfo;
import com.hjm.bookstore.entity.UserScore;
import com.hjm.bookstore.repository.BooksInfoRepository;
import com.hjm.bookstore.repository.UserScoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * 评分服务类
 */
@Service
@Slf4j
public class ScoreService {

    @Autowired
    private UserScoreRepository userScoreRepository;

    @Autowired
    private BooksInfoRepository booksInfoRepository;

    /**
     * 用户评分
     */
    @Transactional
    public UserScore rateBook(Integer userId, Integer bookId, Integer score) {
        if (score < 1 || score > 10) {
            throw new RuntimeException("评分必须在1-10之间");
        }

        // 检查书籍是否存在
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            throw new RuntimeException("书籍不存在");
        }

        // 保存或更新评分
        Optional<UserScore> existingScore = userScoreRepository.findByUserIdAndBookId(userId, bookId);
        UserScore userScore;

        if (existingScore.isPresent()) {
            userScore = existingScore.get();
            userScore.setScore(score);
        } else {
            userScore = new UserScore();
            userScore.setUserId(userId);
            userScore.setBookId(bookId);
            userScore.setScore(score);
        }

        UserScore savedScore = userScoreRepository.save(userScore);

        // 更新书籍平均评分
        updateBookRating(bookId);

        log.info("用户 {} 对书籍 {} 评分: {}", userId, bookId, score);
        return savedScore;
    }

    /**
     * 获取用户对某本书的评分
     */
    public Optional<UserScore> getUserScore(Integer userId, Integer bookId) {
        return userScoreRepository.findByUserIdAndBookId(userId, bookId);
    }

    /**
     * 更新书籍平均评分
     */
    @Transactional
    public void updateBookRating(Integer bookId) {
        Double avgRating = userScoreRepository.getAverageRatingByBookId(bookId);
        
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            BooksInfo book = bookOpt.get();
            if (avgRating != null) {
                book.setRating(BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
            } else {
                book.setRating(BigDecimal.ZERO);
            }
            booksInfoRepository.save(book);
        }
    }
}
