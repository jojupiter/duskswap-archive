package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.deposit.entityDto.DepositDto;
import com.dusk.duskswap.deposit.models.Deposit;
import com.dusk.duskswap.deposit.repositories.DepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepositServiceImpl implements DepositService {

    @Autowired
    private DepositRepository depositRepository;

    @Override
    public ResponseEntity<List<Deposit>> getAllUserDeposits(String userEmail) {
        return null;
    }

    @Override
    public ResponseEntity<List<Deposit>> getAllUserDeposits() {
        return null;
    }

    @Override
    public ResponseEntity<Deposit> createDeposit(DepositDto dto) {
        return null;
    }

    @Override
    public Deposit updateDepositStatus(Long depositId, String statusString) {
        return null;
    }
}
