package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.service.BookService;
import com.github.torleifg.bookquest.core.service.GatewayService;
import com.github.torleifg.bookquest.core.service.StateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
@EnableScheduling
class HarvesterConfig {

    @Value("${scheduler.backoff-seconds}")
    private int backoffSeconds;

    @Bean
    ThreadPoolTaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("harvester-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);

        return scheduler;
    }

    @Bean
    @ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
    public Harvester harvester(List<GatewayService> gateways, BookService bookService, StateService stateService, TransactionTemplate transactionTemplate) {
        if (gateways.isEmpty()) {
            return null;
        }

        return new Harvester(gateways, bookService, stateService, transactionTemplate, backoffSeconds);
    }
}
