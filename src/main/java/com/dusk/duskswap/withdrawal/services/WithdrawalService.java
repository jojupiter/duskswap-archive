package com.dusk.duskswap.withdrawal.services;

import com.dusk.duskswap.usersManagement.models.User;
import com.dusk.duskswap.withdrawal.entityDto.SellDto;
import com.dusk.duskswap.withdrawal.entityDto.SellPriceDto;
import com.dusk.duskswap.withdrawal.entityDto.WithdrawalDto;
import com.dusk.duskswap.withdrawal.entityDto.WithdrawalPage;
import com.dusk.duskswap.withdrawal.models.Sell;
import com.dusk.duskswap.withdrawal.models.Withdrawal;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface WithdrawalService {

    ResponseEntity<WithdrawalPage> getAllUserWithdrawals(Integer currentPage, Integer pageSize);
    ResponseEntity<WithdrawalPage> getAllWithdrawals(Integer currentPage, Integer pageSize);
    Withdrawal createWithdrawal(WithdrawalDto wdto, User user);
    Optional<User> getCurrentUser();

}
