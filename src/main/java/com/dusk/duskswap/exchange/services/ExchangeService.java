package com.dusk.duskswap.exchange.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.exchange.entityDto.ExchangeDto;
import com.dusk.duskswap.exchange.entityDto.ExchangePage;
import com.dusk.duskswap.exchange.models.Exchange;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ExchangeService {

    ResponseEntity<ExchangePage> getAllExchanges(Integer currentPage, Integer pageSize);
    ResponseEntity<ExchangePage> getAllUserExchanges(User user, Integer currentPage, Integer pageSize);
    Exchange makeExchange(ExchangeDto dto, User user, ExchangeAccount exchangeAccount); // here we put exchange account because we don't want to overload this method (exchange account is already got from the controller)

}
