package com.github.torleifg.bookquest.core.service;

public interface GatewayService {

    GatewayResponse find(HarvestState state);

    String getServiceUri();
}
