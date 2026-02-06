package com.github.torleifg.bookquest.core.service;

import com.github.torleifg.bookquest.core.repository.LastModifiedRepository;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class StateService {
    private final ResumptionTokenRepository resumptionTokenRepository;
    private final LastModifiedRepository lastModifiedRepository;

    public StateService(ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        this.resumptionTokenRepository = resumptionTokenRepository;
        this.lastModifiedRepository = lastModifiedRepository;
    }

    public HarvestState get(String serviceUri) {
        return new HarvestState(
                resumptionTokenRepository.get(serviceUri),
                lastModifiedRepository.get(serviceUri)
        );
    }

    public void update(String serviceUri, GatewayResponse response) {
        final String token = response.resumptionToken();

        if (token != null && !token.isBlank()) {
            resumptionTokenRepository.save(serviceUri, token);
        } else {
            resumptionTokenRepository.delete(serviceUri);
        }

        if (response.lastModified() != null) {
            lastModifiedRepository.save(serviceUri, response.lastModified());
        }
    }
}
