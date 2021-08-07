package com.dusk.duskswap.withdrawal.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.withdrawal.entityDto.SellDto;
import com.dusk.duskswap.withdrawal.entityDto.SellPage;
import com.dusk.duskswap.withdrawal.entityDto.SellProfit;
import com.dusk.duskswap.withdrawal.models.Sell;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

public interface SellService {

    ResponseEntity<SellPage> getAllSales(User user, Integer currentPage, Integer pageSize);
    ResponseEntity<SellPage> getAllSales(Integer currentPage, Integer pageSize);
    Sell createSale(SellDto sellDto, User user, ExchangeAccount account) throws Exception; // return sell id, // here we put exchange account because we don't want to overload this method (exchange account is already got from the controller)
                                                                        // exception is used to rollback transaction
    ResponseEntity<String> getAllSaleProfits();
    ResponseEntity<String> getAllSaleProfitsBefore(Date date);
    ResponseEntity<String> getAllSaleProfitsAfter(Date date);
    ResponseEntity<String> getAllSaleProfitsBetween(Date startDate, Date endDate);

}
