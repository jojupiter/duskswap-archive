package com.dusk.duskswap.withdrawal.services;

import com.dusk.duskswap.withdrawal.entityDto.WithdrawalDto;
import com.dusk.duskswap.withdrawal.models.Withdrawal;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface WithdrawalService {

    ResponseEntity<List<Withdrawal>> getAllUserWithdrawals(String userEmail);
    ResponseEntity<List<Withdrawal>> getAllWithdrawals();

    ResponseEntity<Withdrawal> createWithdrawal(WithdrawalDto wdto);
    Withdrawal updateWithdrawalStatus(Long withdrawalId, String statusString);

}
