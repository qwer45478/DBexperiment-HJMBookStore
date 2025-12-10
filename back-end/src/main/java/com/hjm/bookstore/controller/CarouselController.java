package com.hjm.bookstore.controller;

import com.hjm.bookstore.common.Result;
import com.hjm.bookstore.entity.CarouselItem;
import com.hjm.bookstore.service.CarouselService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 轮播图控制器
 */
@RestController
@RequestMapping("/carousel")
@Slf4j
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    /**
     * 获取轮播图列表
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getCarouselList() {
        try {
            List<Map<String, Object>> items = carouselService.getCarouselList();
            return Result.success(items);
        } catch (Exception e) {
            log.error("获取轮播图列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加轮播图
     */
    @PostMapping("/add")
    public Result<CarouselItem> addCarouselItem(@RequestBody Map<String, Integer> request) {
        try {
            Integer bookId = request.get("bookId");
            Integer sortOrder = request.get("sortOrder");
            
            CarouselItem item = carouselService.addCarouselItem(bookId, sortOrder);
            return Result.success("添加成功", item);
        } catch (Exception e) {
            log.error("添加轮播图失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/remove/{id}")
    public Result<Void> removeCarouselItem(@PathVariable Integer id) {
        try {
            carouselService.removeCarouselItem(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除轮播图失败", e);
            return Result.error(e.getMessage());
        }
    }
}
