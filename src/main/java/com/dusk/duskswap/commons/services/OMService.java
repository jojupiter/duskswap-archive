package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.entityDto.OMPaymentPost;
import com.dusk.duskswap.commons.entityDto.OMPaymentResponse;
import com.dusk.duskswap.commons.entityDto.OMTokenBearer;
import org.springframework.http.ResponseEntity;

public interface OMService {

    ResponseEntity<OMTokenBearer> getAccessTokenBearer(String clientCredentials, String authorizationHeader);
    ResponseEntity<OMPaymentResponse> generatePaymentUrl(OMPaymentPost omPaymentPost);

}
