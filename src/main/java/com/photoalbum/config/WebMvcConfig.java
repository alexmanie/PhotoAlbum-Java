/*
* Class Name: WebMvcConfig
* Description: Configures CORS for React development and forwards SPA routes to index.html.
* Date Created: 2026-06-10
*/

package com.photoalbum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        registry.addMapping("/upload")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/detail").setViewName("forward:/index.html");
        registry.addViewController("/detail/{id:[a-zA-Z0-9\\-]+}").setViewName("forward:/index.html");
    }
}
