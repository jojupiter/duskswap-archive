package com.dusk.duskswap.administration.controllers;

import com.dusk.duskswap.commons.entityDto.PricingDto;
import com.dusk.duskswap.commons.models.Level;
import com.dusk.duskswap.commons.models.Pricing;
import com.dusk.duskswap.commons.services.PricingService;
import com.dusk.duskswap.commons.services.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/admin")
public class AdministrationController {

    @Autowired
    private UtilitiesService utilitiesService;
    @Autowired
    private PricingService pricingService;

    // ============================== Levels ========================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/levels/create")
    public ResponseEntity<Level> createLevel(@RequestBody Level level) {
        return utilitiesService.createLevel(level);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/levels/update")
    public ResponseEntity<Level> updateLevel(@RequestParam(name = "levelId") Long levelId, @RequestBody Level newLevel) {
        return utilitiesService.updateLevel(levelId, newLevel);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/levels/all")
    public ResponseEntity<List<Level>> getAllLevels() {
        return utilitiesService.getAllLevels();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/levels/delete")
    public ResponseEntity<Boolean> deleteLevel(@RequestParam(name = "levelId") Long levelId) {
        return utilitiesService.deleteLevel(levelId);
    }

    // =========================== Pricing =========================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/pricing/create")
    public ResponseEntity<Pricing> createPricing(@RequestBody PricingDto pricingDto) {
        return pricingService.createPricing(pricingDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/pricing/update")
    public ResponseEntity<Pricing> updatePricing(@RequestParam(name = "pricingId") Long pricingId, @RequestBody PricingDto pricingDto) {
        return pricingService.updatePricing(pricingId, pricingDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pricing/level-all")
    public ResponseEntity<List<Pricing>> getAllPricingByLevel(@RequestParam(name = "levelId") Long levelId) {
        return pricingService.getAllPricingForLevel(levelId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pricing/currency-all")
    public ResponseEntity<List<Pricing>> getAllPricingByCurrency(@RequestParam(name = "currencyId") Long currencyId) {
        return pricingService.getAllPricingForCurrency(currencyId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pricing/all")
    public ResponseEntity<List<Pricing>> getAllPricing() {
        return pricingService.getAllPricing();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/pricing/delete")
    public ResponseEntity<Boolean> deletePricing(@RequestParam(name = "pricingId") Long pricingId) {
        return pricingService.deletePricing(pricingId);
    }

}