package com.dusk.duskswap.administration.repositories;

import com.dusk.duskswap.administration.models.DefaultConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DefaultConfigRepository extends JpaRepository<DefaultConfig, Long> {

    List<DefaultConfig> findAll();

}
