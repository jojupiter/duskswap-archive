package com.dusk.duskswap.administration.services;

import com.dusk.duskswap.administration.entityDto.DefaultConfigDto;
import com.dusk.duskswap.administration.entityDto.OperationsActivatedDto;
import com.dusk.duskswap.administration.models.DefaultConfig;
import com.dusk.duskswap.administration.models.OperationsActivated;
import com.dusk.duskswap.administration.models.PaymentAPI;
import com.dusk.duskswap.commons.models.Currency;

import java.util.List;
import java.util.Optional;

public interface DefaultConfigService {

    DefaultConfig getConfigs();
    DefaultConfig updateConfigs(DefaultConfigDto config);
    DefaultConfig createConfigs(DefaultConfigDto config);

    PaymentAPI createPaymentAPI(PaymentAPI paymentAPI);
    PaymentAPI updatePaymentAPI(Long paymentAPIId, PaymentAPI newPaymentAPI);
    List<PaymentAPI> getAllPaymentAPIs();

    List<OperationsActivated> initAllowedOperations();
    OperationsActivated updateOperations(OperationsActivatedDto operationsActivatedDto);
    List<OperationsActivated> getAllOperations();
    Optional<OperationsActivated> getOperationsActivatedForCurrency(Currency currency);
    Optional<OperationsActivated> getOperationsActivatedForCurrency(Long currencyId);

}
