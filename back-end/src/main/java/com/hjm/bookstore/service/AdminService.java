package com.hjm.bookstore.service;

import com.hjm.bookstore.entity.AdminInfo;
import com.hjm.bookstore.repository.AdminInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * 管理员服务类
 */
@Service
@Slf4j
public class AdminService {

    @Autowired
    private AdminInfoRepository adminInfoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 创建管理员账号
     */
    @Transactional
    public Map<String, String> createAdmin(Integer adminLevel) {
        // 生成8位管理员ID（字母数字组合）
        String adminId = generateAdminId();
        while (adminInfoRepository.existsByAdminId(adminId)) {
            adminId = generateAdminId();
        }

        // 生成6位密码（字母数字组合）
        String password = generatePassword();

        // 创建管理员
        AdminInfo admin = new AdminInfo();
        admin.setAdminId(adminId);
        admin.setAdminPassword(passwordEncoder.encode(password));
        admin.setAdminLevel(adminLevel);

        adminInfoRepository.save(admin);

        Map<String, String> result = new HashMap<>();
        result.put("adminId", adminId);
        result.put("password", password);

        log.info("创建管理员账号: {}, 等级: {}", adminId, adminLevel);
        return result;
    }

    /**
     * 获取所有管理员
     */
    public List<AdminInfo> getAllAdmins() {
        return adminInfoRepository.findAll();
    }

    /**
     * 修改管理员密码
     */
    @Transactional
    public void changePassword(Map<String, String> request) {
        String adminId = request.get("adminId");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        // 验证参数
        if (adminId == null || oldPassword == null || newPassword == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 查找管理员
        Optional<AdminInfo> adminOpt = adminInfoRepository.findByAdminId(adminId);
        AdminInfo admin = adminOpt.orElseThrow(() -> new IllegalArgumentException("管理员不存在"));

        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, admin.getAdminPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }

        // 验证新密码长度
        if (newPassword.length() < 6 || newPassword.length() > 24) {
            throw new IllegalArgumentException("密码长度为6-24位");
        }

        // 更新密码
        admin.setAdminPassword(passwordEncoder.encode(newPassword));
        adminInfoRepository.save(admin);

        log.info("管理员修改密码成功: {}", adminId);
    }

    /**
     * 删除管理员账号
     */
    @Transactional
    public String deleteAdmin(String targetAdminId, String operatorId) {
        // 验证参数
        if (targetAdminId == null || operatorId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 查找操作者（当前登录的管理员）
        Optional<AdminInfo> operatorOpt = adminInfoRepository.findByAdminId(operatorId);
        AdminInfo operator = operatorOpt.orElseThrow(() -> new IllegalArgumentException("操作者不存在"));

        // 查找目标管理员
        Optional<AdminInfo> targetOpt = adminInfoRepository.findByAdminId(targetAdminId);
        AdminInfo target = targetOpt.orElseThrow(() -> new IllegalArgumentException("目标管理员不存在"));

        // 权限检查：只有2级管理员可以删除1级管理员
        if (operator.getAdminLevel() != 2) {
            throw new IllegalArgumentException("只有超级管理员才能删除其他管理员");
        }

        // 不能删除自己
        if (targetAdminId.equals(operatorId)) {
            throw new IllegalArgumentException("不能删除自己的账号");
        }

        // 只能删除1级管理员
        if (target.getAdminLevel() != 1) {
            throw new IllegalArgumentException("只能删除普通管理员（1级）");
        }

        // 执行删除
        adminInfoRepository.delete(target);

        log.info("管理员删除成功: 操作者={}, 被删除管理员={}", operatorId, targetAdminId);
        return "管理员删除成功";
    }

    /**
     * 生成8位管理员ID
     */
    private String generateAdminId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成6位密码
     */
    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
