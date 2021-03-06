package com.dusk.duskswap.transferring.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.transferring.entityDtos.TransferPage;
import com.dusk.duskswap.transferring.models.Transfer;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TransferService {

    ResponseEntity<TransferPage> getAllTransfers(Integer currentPage, Integer pageSize);
    ResponseEntity<TransferPage> getAllUserTransfers(ExchangeAccount userAccount, Integer currentPage, Integer pageSize);
    Transfer createTransfer(User sender, User recipient, ExchangeAccount fromAccount, ExchangeAccount toAccount, Currency currency, String amount) throws Exception;
    Transfer saveTransfer(Transfer transfer); // After creating a transfer, we save it

}
