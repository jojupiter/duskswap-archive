package com.dusk.duskswap.administration.services;

import com.dusk.duskswap.administration.models.DefaultConfig;
import com.dusk.duskswap.administration.repositories.DefaultConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DefaultConfigServiceImpl implements DefaultConfigService {


    @Autowired
    private DefaultConfigRepository defaultConfigRepository;

    @Override
    public DefaultConfig getConfigs() {
        List<DefaultConfig> defaultConfigList = defaultConfigRepository.findAll();
        if(defaultConfigList != null && !defaultConfigList.isEmpty())
            return defaultConfigList.get(0);

        return null;
    }

    @Override
    public DefaultConfig updateConfigs(DefaultConfig config) {
        if(config == null)
            return null;
        List<DefaultConfig> defaultConfigList = defaultConfigRepository.findAll();
        if(defaultConfigList == null || (defaultConfigList != null && defaultConfigList.isEmpty())) {
            return null;
        }

        if(!defaultConfigList.get(0).getEurToXafBuy().equals(config.getEurToXafBuy()))
            defaultConfigList.get(0).setEurToXafBuy(config.getEurToXafBuy());

        if(!defaultConfigList.get(0).getEurToXafSell().equals(config.getEurToXafSell()))
            defaultConfigList.get(0).setEurToXafSell(config.getEurToXafSell());

        return defaultConfigRepository.save(defaultConfigList.get(0));
    }

    @Override
    public DefaultConfig createConfigs(DefaultConfig defaultConfig) {
        if(defaultConfig == null)
            return null;
        List<DefaultConfig> defaultConfigList = defaultConfigRepository.findAll();
        if(defaultConfigList != null && defaultConfigList.isEmpty()) {
            return null;
        }

        return defaultConfigRepository.save(defaultConfig);
    }
}
