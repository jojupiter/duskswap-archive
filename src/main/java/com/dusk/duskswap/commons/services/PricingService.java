package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.entityDto.PricingDto;
import com.dusk.duskswap.commons.models.Pricing;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PricingService {
    ResponseEntity<Pricing> createPricing(PricingDto dto);
    ResponseEntity<Pricing> updatePricing(Long pricingId, PricingDto dto);
    ResponseEntity<List<Pricing>> getAllPricingForLevel(Long levelId);
    ResponseEntity<List<Pricing>> getAllPricingForCurrency(Long currencyId);
    ResponseEntity<List<Pricing>> getAllPricing();
}
