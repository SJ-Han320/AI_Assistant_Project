package com.bpe.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 프로필 이미지에 대한 정적 리소스 매핑
        // 파일 시스템 경로를 file:// 프로토콜로 매핑
        String fileSystemPath = new File(uploadDir).getAbsolutePath();
        if (!fileSystemPath.endsWith(File.separator)) {
            fileSystemPath += File.separator;
        }
        registry.addResourceHandler("/images/profiles/**")
                .addResourceLocations("file:" + fileSystemPath)
                .addResourceLocations("classpath:/static/images/profiles/");
        
        // 기본 이미지에 대한 정적 리소스 매핑
        registry.addResourceHandler("/images/default/**")
                .addResourceLocations("classpath:/static/images/default/");
    }
}
