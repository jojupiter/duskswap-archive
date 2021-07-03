package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.entityDto.DepositPage;
import com.dusk.duskswap.deposit.entityDto.DepositResponseDto;
import com.dusk.duskswap.deposit.models.Deposit;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DepositService {

    ResponseEntity<DepositPage> getAllUserDeposits(String userToken, Integer currentPage, Integer pageSize);
    ResponseEntity<DepositPage> getAllUserDeposits(Integer currentPage, Integer pageSize);

    Deposit getDepositByInvoiceId(String invoiceId);

    ResponseEntity<DepositResponseDto> createCryptoDeposit(DepositDto dto);
    Deposit updateDepositStatus(Long depositId, String statusString); // destined to be used only when the corresponding invoice is updated by BTCPAY
    ResponseEntity<Boolean> updateDestinationAddress(Long depositId, String toAddress);

}
