package com.dusk.duskswap.administration.services;

import com.dusk.duskswap.administration.entityDto.DefaultConfigDto;
import com.dusk.duskswap.administration.models.DefaultConfig;
import com.dusk.duskswap.administration.models.PaymentAPI;

import java.util.List;

public interface DefaultConfigService {

    DefaultConfig getConfigs();
    DefaultConfig updateConfigs(DefaultConfigDto config);
    DefaultConfig createConfigs(DefaultConfigDto config);

    PaymentAPI createPaymentAPI(PaymentAPI paymentAPI);
    PaymentAPI updatePaymentAPI(Long paymentAPIId, PaymentAPI newPaymentAPI);
    List<PaymentAPI> getAllPaymentAPIs();

}
