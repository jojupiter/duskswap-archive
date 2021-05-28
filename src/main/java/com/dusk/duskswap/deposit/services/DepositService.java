package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.models.Deposit;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DepositService {

    ResponseEntity<List<Deposit>> getAllUserDeposits(String userEmail);
    ResponseEntity<List<Deposit>> getAllUserDeposits();

    ResponseEntity<Deposit> createDeposit(DepositDto dto);
    Deposit updateDepositStatus(Long depositId, String statusString); // destined to be used only when the corresponding invoice is updated by BTCPAY

}
