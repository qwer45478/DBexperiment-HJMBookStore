package com.hjm.bookstore.controller;

import com.hjm.bookstore.common.Result;
import com.hjm.bookstore.service.UserLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户等级控制器
 */
@RestController
@RequestMapping("/user-level")
@Slf4j
public class UserLevelController {
    
    @Autowired
    private UserLevelService userLevelService;
    
    /**
     * 获取用户等级信息
     */
    @GetMapping("/info/{userId}")
    public Result<Map<String, Object>> getUserLevelInfo(@PathVariable Integer userId) {
        try {
            Map<String, Object> levelInfo = userLevelService.getUserLevelInfo(userId);
            if (levelInfo == null) {
                return Result.error("用户不存在");
            }
            return Result.success(levelInfo);
        } catch (Exception e) {
            log.error("获取用户等级信息失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有会员等级说明
     */
    @GetMapping("/descriptions")
    public Result<Map<Integer, Map<String, Object>>> getAllLevelDescriptions() {
        try {
            Map<Integer, Map<String, Object>> descriptions = userLevelService.getAllLevelDescriptions();
            return Result.success(descriptions);
        } catch (Exception e) {
            log.error("获取会员等级说明失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户等级（管理员功能）
     */
    @PostMapping("/update/{userId}")
    public Result<String> updateUserLevel(@PathVariable Integer userId) {
        try {
            userLevelService.updateUserLevel(userId);
            return Result.success("用户等级更新成功");
        } catch (Exception e) {
            log.error("更新用户等级失败", e);
            return Result.error(e.getMessage());
        }
    }
}
