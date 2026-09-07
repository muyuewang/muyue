package com.muyue.common.config;

import com.muyue.common.config.properties.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射（上传文件可通过 /profile/** 访问）
 *
 * @author muyue
 */
@Configuration
@RequiredArgsConstructor
public class ResourcesConfig implements WebMvcConfigurer {

    private final FileProperties fileProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(fileProperties.getUrlPrefix() + "/**")
                .addResourceLocations("file:" + fileProperties.getProfile() + "/");
    }
}
