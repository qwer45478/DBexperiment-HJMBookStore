package com.hjm.bookstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类 - 配置静态资源访问
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置uploads目录下的静态资源访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
        
        // 配置图片资源的访问路径
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/images/");
    }
}
