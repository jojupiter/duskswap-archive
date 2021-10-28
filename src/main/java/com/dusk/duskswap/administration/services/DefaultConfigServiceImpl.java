package com.dusk.duskswap.administration.services;

import com.dusk.duskswap.administration.models.DefaultConfig;
import com.dusk.duskswap.administration.models.PaymentAPI;
import com.dusk.duskswap.administration.repositories.DefaultConfigRepository;
import com.dusk.duskswap.administration.repositories.PaymentAPIRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DefaultConfigServiceImpl implements DefaultConfigService {


    @Autowired
    private DefaultConfigRepository defaultConfigRepository;
    @Autowired
    private PaymentAPIRepository paymentAPIRepository;

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

        // ================ exchange rates ================
        if(
                (
                        config.getUsdToXafBuy() != null && !config.getUsdToXafBuy().isEmpty() &&
                        defaultConfigList.get(0).getUsdToXafBuy() != null &&
                        !defaultConfigList.get(0).getUsdToXafBuy().equals(config.getUsdToXafBuy())
                ) ||
                        defaultConfigList.get(0).getUsdToXafBuy() == null
        )
            defaultConfigList.get(0).setUsdToXafBuy(config.getUsdToXafBuy());

        if(
                (
                        config.getUsdToXafSell() != null && !config.getUsdToXafSell().isEmpty() &&
                        defaultConfigList.get(0).getUsdToXafSell() != null &&
                        !defaultConfigList.get(0).getUsdToXafSell().equals(config.getUsdToXafSell())
                ) ||
                        defaultConfigList.get(0).getUsdToXafSell() == null
        )
            defaultConfigList.get(0).setUsdToXafSell(config.getUsdToXafSell());

        // ================ payment and transfer api used ================
        /*if(
                (
                        config.getOmPaymentAPIUsed() != null && !config.getOmPaymentAPIUsed().isEmpty() &&
                        defaultConfigList.get(0).getOmPaymentAPIUsed() != null &&
                        !defaultConfigList.get(0).getOmPaymentAPIUsed().equals(config.getOmPaymentAPIUsed())
                ) ||
                        defaultConfigList.get(0).getOmPaymentAPIUsed() == null
        )
            defaultConfigList.get(0).setOmPaymentAPIUsed(config.getOmPaymentAPIUsed());

        if(
                (
                        config.getOmTransferAPIUsed() != null && !config.getOmTransferAPIUsed().isEmpty() &&
                        defaultConfigList.get(0).getOmTransferAPIUsed() != null &&
                        !defaultConfigList.get(0).getOmTransferAPIUsed().equals(config.getOmTransferAPIUsed())
                ) ||
                        defaultConfigList.get(0).getOmTransferAPIUsed() == null
        )
            defaultConfigList.get(0).setOmTransferAPIUsed(config.getOmTransferAPIUsed());

        if(
                (
                        config.getMomoPaymentAPIUsed() != null && !config.getMomoPaymentAPIUsed().isEmpty() &&
                        defaultConfigList.get(0).getMomoPaymentAPIUsed() != null &&
                        !defaultConfigList.get(0).getMomoPaymentAPIUsed().equals(config.getMomoPaymentAPIUsed())
                ) ||
                        defaultConfigList.get(0).getMomoPaymentAPIUsed() == null
        )
            defaultConfigList.get(0).setMomoPaymentAPIUsed(config.getMomoPaymentAPIUsed());

        if(
                (
                        config.getMomoTransferAPIUsed() != null && !config.getMomoTransferAPIUsed().isEmpty() &&
                        defaultConfigList.get(0).getMomoTransferAPIUsed() != null &&
                        !defaultConfigList.get(0).getMomoTransferAPIUsed().equals(config.getMomoTransferAPIUsed())
                ) ||
                        defaultConfigList.get(0).getMomoTransferAPIUsed() == null
        )
            defaultConfigList.get(0).setMomoTransferAPIUsed(config.getMomoTransferAPIUsed());*/

        // ================ fees ================
        /*if(
                (
                        config.getCinetpayPaymentFees() != null && !config.getCinetpayPaymentFees().isEmpty() &&
                        defaultConfigList.get(0).getCinetpayPaymentFees() != null &&
                        !defaultConfigList.get(0).getCinetpayPaymentFees().equals(config.getCinetpayPaymentFees())
                ) ||
                        defaultConfigList.get(0).getCinetpayPaymentFees() == null
        )
            defaultConfigList.get(0).setCinetpayPaymentFees(config.getCinetpayPaymentFees());

        if(
                (
                        config.getCinetpayTransferFees() != null && !config.getCinetpayTransferFees().isEmpty() &&
                        defaultConfigList.get(0).getCinetpayTransferFees() != null &&
                        !defaultConfigList.get(0).getCinetpayTransferFees().equals(config.getCinetpayTransferFees())
                ) ||
                        defaultConfigList.get(0).getCinetpayTransferFees() == null
        )
            defaultConfigList.get(0).setCinetpayTransferFees(config.getCinetpayTransferFees());

        if(
                (
                        config.getOmPaymentFees() != null && !config.getOmPaymentFees().isEmpty() &&
                        defaultConfigList.get(0).getOmPaymentFees() != null &&
                        !defaultConfigList.get(0).getOmPaymentFees().equals(config.getOmPaymentFees())
                ) ||
                        defaultConfigList.get(0).getOmPaymentFees() == null
        )
            defaultConfigList.get(0).setOmPaymentFees(config.getOmPaymentFees());

        if(
                (
                        config.getOmTransferFees() != null && !config.getOmTransferFees().isEmpty() &&
                        defaultConfigList.get(0).getOmTransferFees() != null &&
                        !defaultConfigList.get(0).getOmTransferFees().equals(config.getOmTransferFees())
                ) ||
                        defaultConfigList.get(0).getOmTransferFees() == null
        )
            defaultConfigList.get(0).setOmTransferFees(config.getOmTransferFees());

        if(
                (
                        config.getMomoTransferFees() != null && !config.getMomoTransferFees().isEmpty() &&
                        defaultConfigList.get(0).getMomoTransferFees() != null &&
                        !defaultConfigList.get(0).getMomoTransferFees().equals(config.getMomoTransferFees())
                ) ||
                        defaultConfigList.get(0).getMomoTransferFees() == null
        )
            defaultConfigList.get(0).setMomoTransferFees(config.getMomoTransferFees());

        if(
                (
                        config.getMomoTransferFees() != null && !config.getMomoTransferFees().isEmpty() &&
                        defaultConfigList.get(0).getMomoTransferFees() != null &&
                        !defaultConfigList.get(0).getMomoTransferFees().equals(config.getMomoTransferFees())
                ) ||
                        defaultConfigList.get(0).getMomoTransferFees() == null
        )
            defaultConfigList.get(0).setMomoTransferFees(config.getMomoTransferFees());*/

        return defaultConfigRepository.save(defaultConfigList.get(0));
    }

    @Override
    public DefaultConfig createConfigs(DefaultConfig defaultConfig) {
        if(defaultConfig == null)
            return null;
        List<DefaultConfig> defaultConfigList = defaultConfigRepository.findAll();
        if(defaultConfigList != null && !defaultConfigList.isEmpty()) {
            return null;
        }

        /*if(defaultConfig.getOmPaymentAPIUsed() != null && !defaultConfig.getOmPaymentAPIUsed().isEmpty())
            defaultConfig.setOmPaymentAPIUsed(defaultConfig.getOmPaymentAPIUsed().toUpperCase());

        if(defaultConfig.getOmTransferAPIUsed() != null && !defaultConfig.getOmTransferAPIUsed().isEmpty())
            defaultConfig.setOmTransferAPIUsed(defaultConfig.getOmTransferAPIUsed().toUpperCase());

        if(defaultConfig.getMomoPaymentAPIUsed() != null && !defaultConfig.getMomoPaymentAPIUsed().isEmpty())
            defaultConfig.setMomoPaymentAPIUsed(defaultConfig.getMomoPaymentAPIUsed().toUpperCase());

        if(defaultConfig.getMomoTransferAPIUsed() != null && !defaultConfig.getMomoTransferAPIUsed().isEmpty())
            defaultConfig.setMomoTransferAPIUsed(defaultConfig.getMomoTransferAPIUsed().toUpperCase());*/

        return defaultConfigRepository.save(defaultConfig);
    }

    @Override
    public PaymentAPI createPaymentAPI(PaymentAPI paymentAPI) {
        if(paymentAPI == null)
            return null;


        return null;
    }

    @Override
    public PaymentAPI updatePaymentAPI(Long paymentAPIId, PaymentAPI newPaymentAPI) {
        if(paymentAPIId == null || newPaymentAPI == null)
            return null;
        return null;
    }

    @Override
    public List<PaymentAPI> getAllPaymentAPIs() {
        return paymentAPIRepository.findAll();
    }
}
