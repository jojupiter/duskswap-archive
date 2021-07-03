package com.dusk.duskswap.commons.repositories;

import com.dusk.duskswap.commons.models.VerificationCode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;


public interface VerificationCodeRepository extends CrudRepository<VerificationCode, Long> {

    List<VerificationCode> findByUserEmail(String userEmail);

    Optional<VerificationCode> findByUserEmailAndPurpose(String userEmail, String purpose);

    boolean existsByUserEmail(String userEmail);

    boolean existsByUserEmailAndPurpose(String userEmail, String purpose);

    boolean existsByUserEmailAndCodeAndPurpose(String userEmail, Integer code, String purpose);

    //List<VerificationCode> findByUserEmailAndCodeAndPurpose(String userEmail, Integer code, String purpose);

    boolean existsById(Long id);

    //boolean existsByCode(Integer code);

    //List<VerificationCode> findByCode(Integer code);
}
