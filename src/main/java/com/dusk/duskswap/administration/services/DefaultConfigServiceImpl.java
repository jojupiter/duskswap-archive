package com.dusk.duskswap.administration.services;

import com.dusk.duskswap.administration.models.DefaultConfig;
import com.dusk.duskswap.administration.repositories.DefaultConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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

        if(!defaultConfigList.get(0).getUsdToXafBuy().equals(config.getUsdToXafBuy()))
            defaultConfigList.get(0).setUsdToXafBuy(config.getUsdToXafBuy());

        if(!defaultConfigList.get(0).getUsdToXafSell().equals(config.getUsdToXafSell()))
            defaultConfigList.get(0).setUsdToXafSell(config.getUsdToXafSell());

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
