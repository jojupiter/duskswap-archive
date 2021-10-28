package com.dusk.duskswap.administration.repositories;

import com.dusk.duskswap.administration.models.PaymentAPI;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentAPIRepository extends JpaRepository<PaymentAPI, Long> {

    Optional<PaymentAPI> findByApiIso(String apiIso);
    Boolean existsByApiIso(String apiIso);

}
