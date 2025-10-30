package com.bpe.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 프로필 이미지에 대한 정적 리소스 매핑 (로컬 개발 환경)
        registry.addResourceHandler("/images/profiles/**")
                .addResourceLocations("classpath:/static/images/profiles/");
        
        // 기본 이미지에 대한 정적 리소스 매핑
        registry.addResourceHandler("/images/default/**")
                .addResourceLocations("classpath:/static/images/default/");
    }
}
