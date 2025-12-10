package com.hjm.bookstore.controller;

import com.hjm.bookstore.common.Result;
import com.hjm.bookstore.dto.LoginRequest;
import com.hjm.bookstore.dto.RegisterRequest;
import com.hjm.bookstore.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            Map<String, Object> result = authService.register(request);
            return Result.success("注册成功", result);
        } catch (Exception e) {
            log.error("注册失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Map<String, Object> result;
            if ("admin".equals(request.getLoginType())) {
                result = authService.adminLogin(request);
            } else {
                result = authService.userLogin(request);
            }
            return Result.success("登录成功", result);
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error(e.getMessage());
        }
    }
}
