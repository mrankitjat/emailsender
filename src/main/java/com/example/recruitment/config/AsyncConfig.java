package com.example.recruitment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Asynchronous execution configuration defining thread pool bounds for bulk email sending operations.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${email.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${email.async.max-pool-size:15}")
    private int maxPoolSize;

    @Value("${email.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${email.async.thread-name-prefix:EmailAsync-}")
    private String threadNamePrefix;

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
}
