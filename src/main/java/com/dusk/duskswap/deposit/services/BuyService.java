package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.deposit.entityDto.BuyDto;
import com.dusk.duskswap.deposit.entityDto.BuyPage;
import com.dusk.duskswap.deposit.models.Buy;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

public interface BuyService {

    ResponseEntity<BuyPage> getAllBuy(Integer currentPage, Integer pageSize);
    ResponseEntity<BuyPage> getAllBuyByUser(User user, Integer currentPage, Integer pageSize);

    Buy createBuy(User user, BuyDto dto, String payToken, String notifToken);
    Buy updateBuyStatus(String payToken, String statusString);
    String calculateBuyAmount(Long fromCurencyId, Long toCurrency, String fromAmount);

}
