package com.dusk.duskswap.exchange.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.exchange.entityDto.ExchangeDto;
import com.dusk.duskswap.exchange.models.Exchange;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ExchangeService {

    ResponseEntity<List<Exchange>> getAllExchanges();
    ResponseEntity<List<Exchange>> getAllUserExchanges(User user);
    Exchange makeExchange(ExchangeDto dto, User user, ExchangeAccount exchangeAccount);

}
