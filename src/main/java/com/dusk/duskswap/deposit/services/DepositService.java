package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.entityDto.DepositPage;
import com.dusk.duskswap.deposit.entityDto.DepositResponseDto;
import com.dusk.duskswap.deposit.models.Deposit;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface DepositService {

    ResponseEntity<DepositPage> getAllUserDeposits(User user, Integer currentPage, Integer pageSize);
    ResponseEntity<DepositPage> getAllDeposits(Integer currentPage, Integer pageSize);

    Optional<Deposit> getDepositByInvoiceId(String invoiceId);

    ResponseEntity<String/*DepositResponseDto*/> createCryptoDeposit(User user, DepositDto dto) throws Exception; // Exception is used to rollback transactional methods
    Deposit updateDepositStatus(Deposit deposit, String statusString)  throws Exception; // destined to be used only when the corresponding invoice is updated by BTCPAY
    ResponseEntity<Boolean> updateDestinationAddress(Long depositId, String toAddress);

}
