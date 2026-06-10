/*
 * Class Name: AsyncConfig
 * Description: Configures Spring async task executor for background AI description generation.
 * Date Created: 2026-06-10
 */

package com.photoalbum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Enables asynchronous processing and provides a dedicated thread pool
 * for AI-powered photo description generation tasks.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Thread pool executor dedicated to AI description generation.
     * Keeps core threads free for web requests.
     */
    @Bean(name = "descriptionTaskExecutor")
    public Executor descriptionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("DescGen-");
        executor.initialize();
        return executor;
    }
}

