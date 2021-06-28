package com.dusk.duskswap.account.repositories;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.usersManagement.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeAccountRepository extends CrudRepository<ExchangeAccount, Long> {

    List<ExchangeAccount> findAll();
    Optional<ExchangeAccount> findByUser(User user);
    Boolean existsByUser(User user);

}
