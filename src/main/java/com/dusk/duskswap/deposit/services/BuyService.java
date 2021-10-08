package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Level;
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
    Buy createBuy(User user, ExchangeAccount account, BuyDto dto, String payToken, String apiFees, String txId) throws Exception; // api fees in XAF
    Boolean existsByTxId(String txId);
    Buy confirmBuy(Buy buy) throws Exception; // here we calculate fees and the amount that should be attributed
    Buy updateBuyStatus(Buy buy, String statusString);
    Double estimateAmountInCryptoToBeReceived(User user, ExchangeAccount account, Currency currency, String amount);

}
