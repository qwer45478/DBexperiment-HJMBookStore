package com.hjm.bookstore.controller;

import com.hjm.bookstore.common.Result;
import com.hjm.bookstore.entity.AdminInfo;
import com.hjm.bookstore.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 创建管理员账号
     */
    @PostMapping("/create")
    public Result<Map<String, String>> createAdmin(@RequestBody Map<String, Integer> request) {
        try {
            Integer adminLevel = request.get("adminLevel");
            Map<String, String> result = adminService.createAdmin(adminLevel);
            return Result.success("创建成功", result);
        } catch (Exception e) {
            log.error("创建管理员失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有管理员
     */
    @GetMapping("/list")
    public Result<List<AdminInfo>> getAllAdmins() {
        try {
            List<AdminInfo> admins = adminService.getAllAdmins();
            return Result.success(admins);
        } catch (Exception e) {
            log.error("获取管理员列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改管理员密码
     */
    @PutMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> request) {
        try {
            adminService.changePassword(request);
            return Result.success("密码修改成功", null);
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除管理员账号
     */
    @DeleteMapping("/delete/{adminId}")
    public Result<String> deleteAdmin(@PathVariable String adminId, @RequestBody Map<String, String> request) {
        try {
            String operatorId = request.get("operatorId");
            String result = adminService.deleteAdmin(adminId, operatorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除管理员失败", e);
            return Result.error(e.getMessage());
        }
    }
}
