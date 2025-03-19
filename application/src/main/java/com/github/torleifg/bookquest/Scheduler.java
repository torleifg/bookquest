package com.github.torleifg.bookquest;

import com.github.torleifg.bookquest.core.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
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
        if (!enabled || gateways.isEmpty()) {
            log.warn("Scheduler is disabled or no gateways available.");

            return;
        }

        for (final GatewayService gateway : gateways) {
            final String name = gateway.getClass().getSimpleName();

            log.info("Polling {}...", name);

            try {
                while (harvester.poll(gateway)) {
                    log.info("Continue polling {}.", name);
                }
            } catch (Exception e) {
                log.error("Error while polling {}.", name, e);
            } finally {
                log.info("Moving on...");
            }
        }

        log.info("No more data available from any gateway in this cycle.");
    }
}
