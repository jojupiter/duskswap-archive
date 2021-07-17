package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.entityDto.OMPaymentPost;
import com.dusk.duskswap.commons.entityDto.OMPaymentResponse;
import com.dusk.duskswap.commons.entityDto.OMTokenBearer;
import org.springframework.http.ResponseEntity;

public class OMServiceImpl implements OMService {
    @Override
    public ResponseEntity<OMTokenBearer> getAccessTokenBearer(String clientCredentials, String authorizationHeader) {
        return null;
    }

    @Override
    public ResponseEntity<OMPaymentResponse> generatePaymentUrl(OMPaymentPost omPaymentPost) {
        return null;
    }
}
