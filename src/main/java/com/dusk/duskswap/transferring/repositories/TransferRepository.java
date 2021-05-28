package com.dusk.duskswap.transferring.repositories;

import com.dusk.duskswap.transferring.models.Transfer;
import org.springframework.data.repository.CrudRepository;

public interface TransferRepository extends CrudRepository<Transfer, Long> {
}
