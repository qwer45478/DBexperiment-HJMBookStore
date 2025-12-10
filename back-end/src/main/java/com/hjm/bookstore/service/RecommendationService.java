package com.hjm.bookstore.service;

import com.hjm.bookstore.entity.BooksInfo;
import com.hjm.bookstore.entity.UserScore;
import com.hjm.bookstore.repository.BooksInfoRepository;
import com.hjm.bookstore.repository.UserScoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务类 - 基于皮尔逊相似性的协同过滤推荐算法
 */
@Service
@Slf4j
public class RecommendationService {

    @Autowired
    private UserScoreRepository userScoreRepository;

    @Autowired
    private BooksInfoRepository booksInfoRepository;

    /**
     * 为用户推荐书籍
     * @param userId 用户ID
     * @param topN 推荐数量
     * @return 推荐的书籍列表
     */
    public List<BooksInfo> recommendBooks(Integer userId, int topN) {
        // 获取所有用户评分数据
        List<UserScore> allScores = userScoreRepository.findAll();
        
        if (allScores.isEmpty()) {
            // 如果没有评分数据，返回销量最高的书籍
            return booksInfoRepository.findTop10ByStatusOrderBySalesDesc(1)
                    .stream().limit(topN).collect(Collectors.toList());
        }

        // 构建用户-书籍评分矩阵
        Map<Integer, Map<Integer, Integer>> userBookScores = new HashMap<>();
        for (UserScore score : allScores) {
            userBookScores.computeIfAbsent(score.getUserId(), k -> new HashMap<>())
                    .put(score.getBookId(), score.getScore());
        }

        // 获取当前用户的评分
        Map<Integer, Integer> currentUserScores = userBookScores.get(userId);
        if (currentUserScores == null || currentUserScores.isEmpty()) {
            // 如果当前用户没有评分，返回销量最高的书籍
            return booksInfoRepository.findTop10ByStatusOrderBySalesDesc(1)
                    .stream().limit(topN).collect(Collectors.toList());
        }

        // 计算与其他用户的相似度
        Map<Integer, Double> similarities = new HashMap<>();
        for (Integer otherUserId : userBookScores.keySet()) {
            if (!otherUserId.equals(userId)) {
                double similarity = calculatePearsonSimilarity(
                        currentUserScores, 
                        userBookScores.get(otherUserId)
                );
                if (similarity > 0) {
                    similarities.put(otherUserId, similarity);
                }
            }
        }

        // 基于相似用户的评分预测当前用户对未评分书籍的评分
        Map<Integer, Double> predictedScores = new HashMap<>();
        Set<Integer> ratedBooks = currentUserScores.keySet();

        for (Map.Entry<Integer, Double> entry : similarities.entrySet()) {
            Integer similarUserId = entry.getKey();
            Double similarity = entry.getValue();
            Map<Integer, Integer> similarUserScores = userBookScores.get(similarUserId);

            for (Map.Entry<Integer, Integer> bookScore : similarUserScores.entrySet()) {
                Integer bookId = bookScore.getKey();
                Integer score = bookScore.getValue();

                // 只推荐用户未评分的书籍
                if (!ratedBooks.contains(bookId)) {
                    predictedScores.merge(bookId, similarity * score, Double::sum);
                }
            }
        }

        // 获取推荐的书籍ID列表
        List<Integer> recommendedBookIds = predictedScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 获取书籍详情
        List<BooksInfo> recommendedBooks = new ArrayList<>();
        for (Integer bookId : recommendedBookIds) {
            booksInfoRepository.findById(bookId).ifPresent(book -> {
                if (book.getStatus() == 1) {
                    recommendedBooks.add(book);
                }
            });
        }

        // 如果推荐数量不足，用销量高的书籍补充
        if (recommendedBooks.size() < topN) {
            List<BooksInfo> topSalesBooks = booksInfoRepository.findTop10ByStatusOrderBySalesDesc(1);
            for (BooksInfo book : topSalesBooks) {
                if (recommendedBooks.size() >= topN) break;
                if (!ratedBooks.contains(book.getBookId()) && 
                    recommendedBooks.stream().noneMatch(b -> b.getBookId().equals(book.getBookId()))) {
                    recommendedBooks.add(book);
                }
            }
        }

        log.info("为用户 {} 推荐了 {} 本书籍", userId, recommendedBooks.size());
        return recommendedBooks;
    }

    /**
     * 计算皮尔逊相关系数
     */
    private double calculatePearsonSimilarity(Map<Integer, Integer> scores1, Map<Integer, Integer> scores2) {
        // 找出共同评分的书籍
        Set<Integer> commonBooks = new HashSet<>(scores1.keySet());
        commonBooks.retainAll(scores2.keySet());

        if (commonBooks.size() < 2) {
            return 0.0;
        }

        // 计算均值
        double sum1 = 0, sum2 = 0;
        for (Integer bookId : commonBooks) {
            sum1 += scores1.get(bookId);
            sum2 += scores2.get(bookId);
        }
        double mean1 = sum1 / commonBooks.size();
        double mean2 = sum2 / commonBooks.size();

        // 计算皮尔逊相关系数
        double numerator = 0;
        double denominator1 = 0;
        double denominator2 = 0;

        for (Integer bookId : commonBooks) {
            double diff1 = scores1.get(bookId) - mean1;
            double diff2 = scores2.get(bookId) - mean2;
            numerator += diff1 * diff2;
            denominator1 += diff1 * diff1;
            denominator2 += diff2 * diff2;
        }

        if (denominator1 == 0 || denominator2 == 0) {
            return 0.0;
        }

        return numerator / Math.sqrt(denominator1 * denominator2);
    }
}
