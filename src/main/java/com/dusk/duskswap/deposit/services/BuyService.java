package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Pricing;
import com.dusk.duskswap.deposit.entityDto.BuyDto;
import com.dusk.duskswap.deposit.entityDto.BuyPage;
import com.dusk.duskswap.deposit.models.Buy;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface BuyService {

    ResponseEntity<BuyPage> getAllBuy(Integer currentPage, Integer pageSize);
    ResponseEntity<BuyPage> getAllBuyByUser(User user, Integer currentPage, Integer pageSize);

    Optional<Buy> getByTransactionId(String transactionId);
    Buy createBuy(User user, BuyDto dto, String payToken, String apiFees) throws Exception; // api fees in XAF
    Buy confirmBuy(Buy buy) throws Exception; // here we calculate fees and the amount that should be attributed
    Buy updateBuyStatus(Buy buy, String statusString);

}
