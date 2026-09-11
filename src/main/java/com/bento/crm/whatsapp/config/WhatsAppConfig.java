package com.bento.crm.whatsapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Enables the relance scheduler and provides the executor campaign dispatch runs on.
 */
@Configuration
@EnableScheduling
public class WhatsAppConfig {

    /**
     * Campaign dispatch is IO-bound — nearly all of its time is spent waiting on
     * Meta — so a small pool carries a large campaign comfortably.
     *
     * <p>Keeping the pool small is deliberate rather than incidental: it bounds how
     * fast the application can hammer the Graph API, which keeps sends under Meta's
     * throughput limits without a separate rate limiter. The caller-runs policy
     * means that if a burst of campaigns fills the queue, the launching thread
     * absorbs the work instead of the campaign being silently dropped.
     */
    @Bean
    public TaskExecutor whatsAppTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("wa-dispatch-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
