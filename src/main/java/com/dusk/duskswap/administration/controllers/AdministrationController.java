package com.dusk.duskswap.administration.controllers;

import com.dusk.duskswap.administration.entityDto.DefaultConfigDto;
import com.dusk.duskswap.administration.models.DefaultConfig;
import com.dusk.duskswap.administration.models.OverallBalance;
import com.dusk.duskswap.administration.models.PaymentAPI;
import com.dusk.duskswap.administration.services.DefaultConfigService;
import com.dusk.duskswap.administration.services.OverallBalanceService;
import com.dusk.duskswap.commons.entityDto.PricingDto;
import com.dusk.duskswap.commons.miscellaneous.Codes;
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
    @Autowired
    private OverallBalanceService overallBalanceService;
    @Autowired
    private DefaultConfigService defaultConfigService;

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

    // ================================= Available funds ==========================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/balance/initialize")
    public ResponseEntity<?> initializeBalances() {
        return overallBalanceService.initializeBalances();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/balance/create")
    public ResponseEntity<?> createBalance(@RequestParam(name = "currencyId") Long currencyId) {
        return overallBalanceService.createBalanceFor(currencyId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/balance/increase")
    public ResponseEntity<?> increaseOverallAmount(
            @RequestParam(name = "amount") String amountToIncrease,
            @RequestParam(name = "currencyId") Long currencyId,
            @RequestParam(name = "depositOrWithdrawal") Integer depositOrWithdrawal
    ) {
        return overallBalanceService.increaseAmount(amountToIncrease, currencyId, depositOrWithdrawal);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/balance/decrease")
    public ResponseEntity<?> decreaseOverallAmount(
            @RequestParam(name = "amount") String amountToDecrease,
            @RequestParam(name = "currencyId") Long currencyId,
            @RequestParam(name = "depositOrWithdrawal") Integer depositOrWithdrawal
    ) {
        return overallBalanceService.decreaseAmount(amountToDecrease, currencyId, depositOrWithdrawal);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/balance/update")
    public ResponseEntity<?> updateBalance(
            @RequestParam(name = "amount") String amount,
            @RequestParam(name = "currencyId") Long currencyId,
            @RequestParam(name = "depositOrWithdrawal") Integer depositOrWithdrawal
    ) {
        return overallBalanceService.updateBalanceFor(currencyId, amount, depositOrWithdrawal);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/balance/all")
    public ResponseEntity<List<OverallBalance>> getAllBalances() {
        return overallBalanceService.getAllBalances();
    }

    // ================================ Configs ==================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/configs/create")
    public ResponseEntity<?> createConfigs(@RequestBody DefaultConfigDto config) {
        if(config == null) {
            return ResponseEntity.badRequest().body(Codes.INPUT_ERROR_CODE);
        }
        return ResponseEntity.ok(defaultConfigService.createConfigs(config));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/configs/update")
    public ResponseEntity<?> updateConfigs(@RequestBody DefaultConfigDto config) {
        if(config == null) {
            return ResponseEntity.badRequest().body(Codes.INPUT_ERROR_CODE);
        }
        return ResponseEntity.ok(defaultConfigService.updateConfigs(config));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/configs")
    public ResponseEntity<DefaultConfig> getConfigs() {
        return ResponseEntity.ok(defaultConfigService.getConfigs());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/configs/api/create")
    public ResponseEntity<?> createPaymentAPI(@RequestBody PaymentAPI paymentAPI) {
        if(paymentAPI == null)
            return ResponseEntity.badRequest().body(null);

        return ResponseEntity.ok(defaultConfigService.createPaymentAPI(paymentAPI));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/configs/api/update")
    public ResponseEntity<?> updatePaymentAPI(@RequestParam(name = "apiId") Long apiId,
                                              @RequestBody PaymentAPI paymentAPI) {
        if(apiId == null || paymentAPI == null)
            return ResponseEntity.badRequest().body(null);

        return ResponseEntity.ok(defaultConfigService.updatePaymentAPI(apiId, paymentAPI));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/configs/api/all")
    public ResponseEntity<?> getAllPaymentAPIs() {
        return ResponseEntity.ok(defaultConfigService.getAllPaymentAPIs());
    }

}
