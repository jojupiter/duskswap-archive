package com.dusk.duskswap.withdrawal.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.withdrawal.entityDto.SellDto;
import com.dusk.duskswap.withdrawal.entityDto.SellPage;
import com.dusk.duskswap.withdrawal.entityDto.SellProfit;
import com.dusk.duskswap.withdrawal.models.Sell;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface SellService {

    ResponseEntity<SellPage> getAllSell(User user, Integer currentPage, Integer pageSize);
    ResponseEntity<SellPage> getAllSell(Integer currentPage, Integer pageSize);
    Sell createSell(SellDto sellDto, User user, ExchangeAccount account, Currency fromCurrency,
                    TransactionOption transactionOption, String usdXaf, String apiFees) throws Exception; // return sell id, // here we put exchange account because we don't want to overload this method (exchange account is already got from the controller)
                                                                        // exception is used to rollback transaction
    Sell saveSell(Sell sell, String apiTransactionId);
    Sell updateSellStatus(Sell sell, String statusString);
    Optional<Sell> getSellByTransactionId(String transactionId);
    ResponseEntity<String> getAllSellProfits();
    ResponseEntity<String> getAllSellProfitsBefore(Date date);
    ResponseEntity<String> getAllSellProfitsAfter(Date date);
    ResponseEntity<String> getAllSellProfitsBetween(Date startDate, Date endDate);

}
