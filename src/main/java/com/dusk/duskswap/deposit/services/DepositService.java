package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.commons.models.InvoicePayment;
import com.dusk.duskswap.commons.models.Payment;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.deposit.entityDto.*;
import com.dusk.duskswap.deposit.models.Deposit;
import com.dusk.duskswap.deposit.models.DepositHash;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface DepositService {

    ResponseEntity<DepositPage> getAllUserDeposits(User user, Integer currentPage, Integer pageSize);
    ResponseEntity<DepositPage> getAllDeposits(Integer currentPage, Integer pageSize);
    Optional<Deposit> getDepositById(Long depositId);
    Optional<Deposit> getDepositByInvoiceId(String invoiceId);

    ResponseEntity<String/*DepositResponseDto*/> createCryptoDeposit(User user, DepositDto dto) throws Exception; // Exception is used to rollback transactional methods
    Deposit updateDepositStatus(Deposit deposit, String statusString, String paidAmount)  throws Exception; // destined to be used only when the corresponding invoice is updated by BTCPAY
    ResponseEntity<Boolean> updateDestinationAddress(Long depositId, String toAddress);

    ResponseEntity<DepositHashPage> getAllUserDepositHashes(ExchangeAccount account, Integer currentPage, Integer pageSize);
    Optional<DepositHash> getDepositHashByTransaction(String transactionHash);
    Boolean createDepositHash(List<InvoicePayment> invoicePayments, Deposit deposit) throws Exception;
    DepositHash updateDepositHashStatus(DepositHash depositHash, String statusString);
    DepositHashCount countDepositHashes(String invoiceId);
}
