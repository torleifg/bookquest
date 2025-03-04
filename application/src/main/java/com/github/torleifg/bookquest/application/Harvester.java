package com.github.torleifg.bookquest.application;

import com.github.torleifg.bookquest.application.service.MetadataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Harvester {
    private final MetadataService metadataService;

    @Value("${harvesting.enabled}")
    private boolean enabled;

    public Harvester(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Scheduled(initialDelayString = "${harvesting.initial-delay}", fixedDelayString = "${harvesting.fixed-delay}", timeUnit = TimeUnit.SECONDS)
    public void poll() {
        if (!enabled) {
            return;
        }

        while (true) {
            final boolean continuePoll = metadataService.findAndSave();

            if (!continuePoll) {
                break;
            }
        }
    }
}
