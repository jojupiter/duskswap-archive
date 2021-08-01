package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.entityDto.DepositPage;
import com.dusk.duskswap.deposit.entityDto.DepositResponseDto;
import com.dusk.duskswap.deposit.models.Deposit;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DepositService {

    ResponseEntity<DepositPage> getAllUserDeposits(User user, Integer currentPage, Integer pageSize);
    ResponseEntity<DepositPage> getAllDeposits(Integer currentPage, Integer pageSize);

    Deposit getDepositByInvoiceId(String invoiceId);

    ResponseEntity<DepositResponseDto> createCryptoDeposit(User user, DepositDto dto);
    Deposit updateDepositStatus(Long depositId, String statusString); // destined to be used only when the corresponding invoice is updated by BTCPAY
    ResponseEntity<Boolean> updateDestinationAddress(Long depositId, String toAddress);

}
