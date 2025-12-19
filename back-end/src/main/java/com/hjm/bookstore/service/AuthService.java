package com.hjm.bookstore.service;

import com.hjm.bookstore.dto.LoginRequest;
import com.hjm.bookstore.dto.RegisterRequest;
import com.hjm.bookstore.entity.AdminInfo;
import com.hjm.bookstore.entity.UserInfo;
import com.hjm.bookstore.repository.AdminInfoRepository;
import com.hjm.bookstore.repository.UserInfoRepository;
import com.hjm.bookstore.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 认证服务类
 */
@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private AdminInfoRepository adminInfoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CouponService couponService;

    /**
     * 用户注册
     */
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        // 检查电话号码是否已存在
        if (userInfoRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("该电话号码已被注册");
        }

        // 创建新用户
        UserInfo user = new UserInfo();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserLevel(0);
        // 用户等级默认为0，会根据消费金额自动提升

        UserInfo savedUser = userInfoRepository.save(user);

        // 生成Token
        String token = jwtUtil.generateToken(savedUser.getUserId().toString(), "user");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", savedUser.getUserId());
        result.put("username", savedUser.getUsername());
        result.put("userLevel", savedUser.getUserLevel());

        log.info("用户注册成功: {}", savedUser.getPhone());
        return result;
    }

    /**
     * 用户登录
     */
    public Map<String, Object> userLogin(LoginRequest request) {
        // 查找用户
        Optional<UserInfo> userOpt = userInfoRepository.findByPhone(request.getAccount());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        UserInfo user = userOpt.get();

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 5级用户自动领取每日优惠券（先判断是否应该下发）
        if (user.getUserLevel() >= 5) {
            try {
                boolean claimed = couponService.claimDailyCoupon(user.getUserId());
                if (claimed) {
                    log.info("5级用户{}自动领取每日优惠券成功", user.getUserId());
                }
            } catch (Exception e) {
                log.warn("5级用户{}自动领取每日优惠券失败: {}", user.getUserId(), e.getMessage());
                // 不影响登录流程，只记录警告日志
            }
        }

        // 记录登录时间（在判断优惠券领取之后）
        userInfoRepository.updateLatestLog(user.getUserId());

        // 生成Token
        String token = jwtUtil.generateToken(user.getUserId().toString(), "user");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("userLevel", user.getUserLevel());
        result.put("phone", user.getPhone());

        log.info("用户登录成功: {}", user.getPhone());
        return result;
    }

    /**
     * 管理员登录
     */
    public Map<String, Object> adminLogin(LoginRequest request) {
        // 查找管理员
        Optional<AdminInfo> adminOpt = adminInfoRepository.findByAdminId(request.getAccount());
        if (adminOpt.isEmpty()) {
            throw new RuntimeException("管理员不存在");
        }

        AdminInfo admin = adminOpt.get();

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), admin.getAdminPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成Token
        String token = jwtUtil.generateToken(admin.getAdminId(), "admin");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("adminId", admin.getAdminId());
        result.put("adminLevel", admin.getAdminLevel());

        log.info("管理员登录成功: {}", admin.getAdminId());
        return result;
    }
}
