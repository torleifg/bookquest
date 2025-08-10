package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
class Scheduler {
    private final List<GatewayService> gateways;
    private final Harvester harvester;

    private final boolean enabled;

    Scheduler(List<GatewayService> gateways, Harvester harvester, @Value("${scheduler.enabled}") boolean enabled) {
        this.gateways = gateways;
        this.harvester = harvester;
        this.enabled = enabled;
    }

    @Scheduled(initialDelayString = "${scheduler.initial-delay}", fixedDelayString = "${scheduler.fixed-delay}", timeUnit = TimeUnit.SECONDS)
    void run() {
        if (!enabled) {
            log.warn("Scheduler is disabled.");
            return;
        }

        if (gateways.isEmpty()) {
            log.warn("No gateways available.");
            return;
        }

        final Queue<GatewayService> queue = new ArrayDeque<>(gateways);

        while (!queue.isEmpty()) {
            final GatewayService gateway = queue.poll();
            final String name = gateway.getClass().getSimpleName();

            log.info("Polling {}...", name);

            try {
                if (harvester.poll(gateway)) {
                    queue.add(gateway);
                } else {
                    log.info("Finished polling {}", name);
                }
            } catch (Exception e) {
                log.error("Error while polling {}.", name, e);
            }
        }
    }
}
