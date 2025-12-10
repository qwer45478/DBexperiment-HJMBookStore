package com.hjm.bookstore.controller;

import com.hjm.bookstore.common.Result;
import com.hjm.bookstore.entity.UserScore;
import com.hjm.bookstore.service.ScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.Map;

/**
 * 评分控制器
 */
@RestController
@RequestMapping("/scores")
@Slf4j
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    /**
     * 用户评分
     */
    @PostMapping("/rate")
    public Result<UserScore> rateBook(@RequestBody Map<String, Integer> request) {
        try {
            Integer userId = request.get("userId");
            Integer bookId = request.get("bookId");
            Integer score = request.get("score");

            UserScore userScore = scoreService.rateBook(userId, bookId, score);
            return Result.success("评分成功", userScore);
        } catch (Exception e) {
            log.error("评分失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户对某本书的评分
     */
    @GetMapping("/{userId}/{bookId}")
    public Result<UserScore> getUserScore(@PathVariable Integer userId, @PathVariable Integer bookId) {
        try {
            Optional<UserScore> userScore = scoreService.getUserScore(userId, bookId);
            if (userScore.isPresent()) {
                return Result.success(userScore.get());
            } else {
                // 用户未评分，返回成功但data为null
                return Result.success(null);
            }
        } catch (Exception e) {
            log.error("获取评分失败", e);
            return Result.error(e.getMessage());
        }
    }
}
