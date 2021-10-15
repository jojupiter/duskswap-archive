package com.dusk.duskswap.administration.services;

import com.dusk.duskswap.administration.models.DefaultConfig;

public interface DefaultConfigService {

    public DefaultConfig getConfigs();
    public DefaultConfig updateConfigs(DefaultConfig config);
    public DefaultConfig createConfigs(DefaultConfig defaultConfig);

}
