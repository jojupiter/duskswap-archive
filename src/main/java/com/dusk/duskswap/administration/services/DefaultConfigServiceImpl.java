package com.dusk.duskswap.administration.services;

import com.dusk.duskswap.administration.entityDto.DefaultConfigDto;
import com.dusk.duskswap.administration.models.DefaultConfig;
import com.dusk.duskswap.administration.models.PaymentAPI;
import com.dusk.duskswap.administration.repositories.DefaultConfigRepository;
import com.dusk.duskswap.administration.repositories.PaymentAPIRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public DefaultConfig updateConfigs(DefaultConfigDto configDto) {
        if(configDto == null)
            return null;
        List<DefaultConfig> defaultConfigList = defaultConfigRepository.findAll();
        if(defaultConfigList == null || (defaultConfigList != null && defaultConfigList.isEmpty())) {
            return null;
        }

        // ================ exchange rates ================
        if(
                (
                        configDto.getUsdToXafBuy() != null && !configDto.getUsdToXafBuy().isEmpty() &&
                        defaultConfigList.get(0).getUsdToXafBuy() != null &&
                        !defaultConfigList.get(0).getUsdToXafBuy().equals(configDto.getUsdToXafBuy())
                ) ||
                        defaultConfigList.get(0).getUsdToXafBuy() == null
        )
            defaultConfigList.get(0).setUsdToXafBuy(configDto.getUsdToXafBuy());

        if(
                (
                        configDto.getUsdToXafSell() != null && !configDto.getUsdToXafSell().isEmpty() &&
                        defaultConfigList.get(0).getUsdToXafSell() != null &&
                        !defaultConfigList.get(0).getUsdToXafSell().equals(configDto.getUsdToXafSell())
                ) ||
                        defaultConfigList.get(0).getUsdToXafSell() == null
        )
            defaultConfigList.get(0).setUsdToXafSell(configDto.getUsdToXafSell());

        // ================ payment and transfer api used ================
        Optional<PaymentAPI> omPaymentApi = null;
        Optional<PaymentAPI> momoPaymentApi = null;
        if(
                configDto.getOmPaymentApiUsedIso() != null &&
                !configDto.getOmPaymentApiUsedIso().isEmpty()
        ) {
            omPaymentApi = paymentAPIRepository.findByApiIso(configDto.getOmPaymentApiUsedIso());
            if(omPaymentApi.isPresent())
                defaultConfigList.get(0).setOmAPIUsed(omPaymentApi.get());
        }

        if(
                configDto.getMomoPaymentApiUsedIso() != null &&
                !configDto.getMomoPaymentApiUsedIso().isEmpty()
        ) {
            momoPaymentApi = paymentAPIRepository.findByApiIso(configDto.getMomoPaymentApiUsedIso());
            if(momoPaymentApi.isPresent())
                defaultConfigList.get(0).setMomoAPIUsed(momoPaymentApi.get());
        }

        return defaultConfigRepository.save(defaultConfigList.get(0));
    }

    @Override
    public DefaultConfig createConfigs(DefaultConfigDto configDto) {
        if(configDto == null)
            return null;
        List<DefaultConfig> defaultConfigList = defaultConfigRepository.findAll();
        if(defaultConfigList != null && !defaultConfigList.isEmpty()) {
            return null;
        }

        Optional<PaymentAPI> omPaymentApi = null;
        Optional<PaymentAPI> momoPaymentApi = null;

        if(configDto.getOmPaymentApiUsedIso() != null)
            omPaymentApi = paymentAPIRepository.findByApiIso(configDto.getOmPaymentApiUsedIso());
        if(configDto.getMomoPaymentApiUsedIso() != null)
            momoPaymentApi = paymentAPIRepository.findByApiIso(configDto.getMomoPaymentApiUsedIso());

        DefaultConfig config = new DefaultConfig();
        if(omPaymentApi.isPresent())
            config.setOmAPIUsed(omPaymentApi.get());
        if(momoPaymentApi.isPresent())
            config.setMomoAPIUsed(momoPaymentApi.get());
        config.setUsdToXafBuy(configDto.getUsdToXafBuy());
        config.setUsdToXafSell(configDto.getUsdToXafSell());

        return defaultConfigRepository.save(config);
    }

    @Override
    public PaymentAPI createPaymentAPI(PaymentAPI paymentAPI) {
        if(paymentAPI == null || (paymentAPI != null && paymentAPI.getApiIso().isEmpty()))
            return null;

        if(
                paymentAPIRepository.existsByApiIso(paymentAPI.getApiIso()) ||
                (paymentAPI.getId() != null && paymentAPIRepository.existsById(paymentAPI.getId()))
        )
            return null;

        if(
                (paymentAPI.getPaymentFees() != null && !paymentAPI.getPaymentFees().isEmpty() && Double.parseDouble(paymentAPI.getPaymentFees()) < 0) ||
                (paymentAPI.getTransferFees() != null && !paymentAPI.getTransferFees().isEmpty() && Double.parseDouble(paymentAPI.getTransferFees()) < 0)
        )
            return null;

        return paymentAPIRepository.save(paymentAPI);
    }

    @Override
    public PaymentAPI updatePaymentAPI(Long paymentAPIId, PaymentAPI newPaymentAPI) {
        if(paymentAPIId == null || newPaymentAPI == null)
            return null;

        if(
                (newPaymentAPI.getPaymentFees() != null && !newPaymentAPI.getPaymentFees().isEmpty() && Double.parseDouble(newPaymentAPI.getPaymentFees()) < 0) ||
                (newPaymentAPI.getTransferFees() != null && !newPaymentAPI.getTransferFees().isEmpty() && Double.parseDouble(newPaymentAPI.getTransferFees()) < 0)
        )
            return null;

        Optional<PaymentAPI> oldPaymentAPI = paymentAPIRepository.findById(paymentAPIId);
        if(!oldPaymentAPI.isPresent())
            return null;

        if(
                newPaymentAPI.getPaymentFees() != null &&
                !newPaymentAPI.getPaymentFees().isEmpty() &&
                !newPaymentAPI.getPaymentFees().equals(oldPaymentAPI.get().getPaymentFees())
        )
            oldPaymentAPI.get().setPaymentFees(newPaymentAPI.getPaymentFees());

        if(
                newPaymentAPI.getTransferFees() != null &&
                !newPaymentAPI.getTransferFees().isEmpty() &&
                !newPaymentAPI.getTransferFees().equals(oldPaymentAPI.get().getTransferFees())
        )
            oldPaymentAPI.get().setTransferFees(newPaymentAPI.getTransferFees());

        if(
                newPaymentAPI.getApiIso() != null &&
                !newPaymentAPI.getApiIso().isEmpty() &&
                !newPaymentAPI.getApiIso().equals(oldPaymentAPI.get().getApiIso())
        )
            oldPaymentAPI.get().setApiIso(newPaymentAPI.getApiIso());

        if(
                newPaymentAPI.getApiFullName() != null &&
                !newPaymentAPI.getApiFullName().isEmpty() &&
                !newPaymentAPI.getApiFullName().equals(oldPaymentAPI.get().getApiFullName())
        )
            oldPaymentAPI.get().setApiFullName(newPaymentAPI.getApiFullName());

        return paymentAPIRepository.save(oldPaymentAPI.get());
    }

    @Override
    public List<PaymentAPI> getAllPaymentAPIs() {
        return paymentAPIRepository.findAll();
    }
}
