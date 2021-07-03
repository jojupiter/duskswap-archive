package com.dusk.duskswap.withdrawal.services;

import com.dusk.duskswap.withdrawal.entityDto.SellDto;
import com.dusk.duskswap.withdrawal.entityDto.SellPriceDto;
import com.dusk.duskswap.withdrawal.models.Sell;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface SellService {

    ResponseEntity<List<Sell>> getAllSales(String userEmail);
    ResponseEntity<List<Sell>> getAllSales();
    ResponseEntity<SellPriceDto> calculateSale(SellDto sellDto);
    Sell createSale(SellDto sellDto); // return sell id
    //ResponseEntity<Boolean> confirmSale(Long sellId);

}
