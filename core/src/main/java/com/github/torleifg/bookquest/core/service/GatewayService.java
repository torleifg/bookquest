package com.github.torleifg.bookquest.core.service;

public interface GatewayService {

    GatewayResponse find();

    void updateHarvestState(GatewayResponse response);
}
