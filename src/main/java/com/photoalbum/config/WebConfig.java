/*
 * Class Name: WebConfig
 * Description: Forwards client-side SPA routes to index.html so the React Router handles deep links.
 * Date Created: 2026-06-10
 */

package com.photoalbum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures forwarding of React Router deep-link routes to the SPA entry point.
 *
 * <p>Requests for client-side routes such as {@code /detail/{id}} must return the
 * generated {@code index.html} so the browser can boot React and let React Router
 * resolve the route. API ({@code /api/**}) and asset requests are unaffected
 * because they are matched by their own handlers/resources first.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forward known SPA routes to the React entry point.
        registry.addViewController("/detail/{id}").setViewName("forward:/index.html");
        registry.addViewController("/detail").setViewName("forward:/index.html");
    }
}
