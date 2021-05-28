package com.dusk.shared.commons.repositories;

import com.dusk.shared.commons.models.VerificationCode;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface VerificationCodeRepository extends CrudRepository<VerificationCode, Long> {

    List<VerificationCode> findByUserEmail(String userEmail);

    boolean existsByUserEmail(String userEmail);

    boolean existsByUserEmailAndCode(String userEmail, Integer code);

    boolean existsById(Long id);

    boolean existsByCode(Integer code);

    List<VerificationCode> findByCode(Integer code);
}
