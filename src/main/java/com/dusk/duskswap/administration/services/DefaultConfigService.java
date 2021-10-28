package com.dusk.duskswap.administration.services;

import com.dusk.duskswap.administration.models.DefaultConfig;
import com.dusk.duskswap.administration.models.PaymentAPI;

import java.util.List;

public interface DefaultConfigService {

    DefaultConfig getConfigs();
    DefaultConfig updateConfigs(DefaultConfig config);
    DefaultConfig createConfigs(DefaultConfig defaultConfig);

    PaymentAPI createPaymentAPI(PaymentAPI paymentAPI);
    PaymentAPI updatePaymentAPI(Long paymentAPIId, PaymentAPI newPaymentAPI);
    List<PaymentAPI> getAllPaymentAPIs();

}
