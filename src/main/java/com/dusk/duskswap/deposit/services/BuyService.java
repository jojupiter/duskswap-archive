package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Pricing;
import com.dusk.duskswap.deposit.entityDto.BuyDto;
import com.dusk.duskswap.deposit.entityDto.BuyPage;
import com.dusk.duskswap.deposit.models.Buy;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

public interface BuyService {

    ResponseEntity<BuyPage> getAllBuy(Integer currentPage, Integer pageSize);
    ResponseEntity<BuyPage> getAllBuyByUser(User user, Integer currentPage, Integer pageSize);

    Buy createBuy(User user, BuyDto dto, String payToken, String notifToken, String apiFees) throws Exception; // api fees in XAF
    Buy updateBuy(String notifToken, String statusString) throws Exception; // here we calculate fees and the amount that should be attributed

}
