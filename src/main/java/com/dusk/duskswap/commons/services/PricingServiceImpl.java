package com.dusk.duskswap.commons.services;

import com.dusk.duskswap.commons.entityDto.PricingDto;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Level;
import com.dusk.duskswap.commons.models.Pricing;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.commons.repositories.LevelRepository;
import com.dusk.duskswap.commons.repositories.PricingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PricingServiceImpl implements PricingService {
    @Autowired
    private PricingRepository pricingRepository;
    @Autowired
    private LevelRepository levelRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    private Logger logger = LoggerFactory.getLogger(PricingServiceImpl.class);

    @Override
    public ResponseEntity<Pricing> createPricing(PricingDto dto) {
        // input checking
        if(dto == null) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> createPricing :: PricingServiceImpl.java ");
            return ResponseEntity.badRequest().body(null);
        }

        // here we get the level and currency of the pricing
        Optional<Level> level = levelRepository.findById(dto.getLevelId());
        if(!level.isPresent()) {
            logger.error("[" + new Date() + "] => LEVEL NOT FOUND >>>>>>>> createPricing :: PricingServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Optional<Currency> currency = currencyRepository.findById(dto.getCurrencyId());
        if(!currency.isPresent()) {
            logger.error("[" + new Date() + "] => CURRENCY NOT FOUND >>>>>>>> createPricing :: PricingServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // next we check if a pricing with the same user's level and currency exists. If yes, then we don't create it anymore
        if(pricingRepository.existsByLevelAndCurrency(level.get(), currency.get())) {
            logger.info("[" + new Date() + "] => PRICING ALREADY EXISTS >>>>>>>> createPricing :: PricingServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // next we check the type
        if(dto.getType() == null || (dto.getType() != null && dto.getType().isEmpty()))
            dto.setType(DefaultProperties.PRICING_TYPE_FIX); // by default, we set pricing type to fix

        if(dto.getType().equals(DefaultProperties.PRICING_TYPE_PERCENTAGE)) {
            if(
                    Double.parseDouble(dto.getBuyFees()) > 1.0 || Double.parseDouble(dto.getBuyFees()) < 0.0 ||
                    Double.parseDouble(dto.getSellFees()) > 1.0 || Double.parseDouble(dto.getSellFees()) < 0.0 ||
                    Double.parseDouble(dto.getExchangeFees()) > 1.0 || Double.parseDouble(dto.getExchangeFees()) < 0.0
            ) {
                logger.info("[" + new Date() + "] => PRICING FEES PERCENTAGE OUT OF BOUND [0.0, 1.0] >>>>>>>> createPricing :: PricingServiceImpl.java");
                return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        if(
                !dto.getType().equals(DefaultProperties.PRICING_TYPE_FIX) &&
                !dto.getType().equals(DefaultProperties.PRICING_TYPE_PERCENTAGE)
        ) { // If type is not fix or percentage, then we return an error
            logger.info("[" + new Date() + "] => PRICING TYPE IS NEITHER FIX NOR PERCENTAGE >>>>>>>> createPricing :: PricingServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // then we create properly the pricing
        Pricing pricing = new Pricing();
        pricing.setLevel(level.get());
        pricing.setCurrency(currency.get());
        pricing.setType(dto.getType().toUpperCase());
        pricing.setBuyFees(dto.getBuyFees());
        pricing.setBuyMax(dto.getBuyMax());
        pricing.setBuyMin(dto.getBuyMin());
        pricing.setDepositMin(dto.getDepositMin());
        pricing.setDepositMax(dto.getDepositMax());
        pricing.setWithdrawalFees(dto.getWithdrawalFees());
        pricing.setWithdrawalMin(dto.getWithdrawalMin());
        pricing.setWithdrawalMax(dto.getWithdrawalMax());
        pricing.setSellFees(dto.getSellFees());
        pricing.setSellMax(dto.getSellMax());
        pricing.setSellMin(dto.getSellMin());
        pricing.setExchangeFees(dto.getExchangeFees());
        pricing.setExchangeMin(dto.getExchangeMin());
        pricing.setExchangeMax(dto.getExchangeMax());

        // here we check the positivity of numbers
        if(!isPricingPositive(pricing)) {
            logger.error("[" + new Date() + "] => PRICING CONTAINS NEGATIVE VALUE >>>>>>>> createPricing :: PricingServiceImpl.java " +
                    " ===== pricing = " + pricing);
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(pricingRepository.save(pricing));
    }

    @Override
    public ResponseEntity<Pricing> updatePricing(Long pricingId, PricingDto dto) {
        // input checking
        if(pricingId == null || dto == null) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> updatePricing :: PricingServiceImpl.java " +
                    "========= pricingId = " + pricingId + ",  pricingDto = " + dto);
            return ResponseEntity.badRequest().body(null);
        }

        // now we get the corresponding pricing
        Optional<Pricing> pricing = pricingRepository.findById(pricingId);
        if(!pricing.isPresent()) {
            logger.error("[" + new Date() + "] => PRICING NOT FOUND >>>>>>>> updatePricing :: PricingServiceImpl.java ");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // here we check the positivity of numbers
        if(!isPricingPositive(pricing.get())) {
            logger.error("[" + new Date() + "] => PRICING CONTAINS NEGATIVE VALUE >>>>>>>> updatePricing :: PricingServiceImpl.java " +
                    " ===== pricing = " + pricing.get());
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(dto.getType() != null && !dto.getType().equals(pricing.get().getType()))
            pricing.get().setType(dto.getType());

        if(dto.getBuyFees() != null && !dto.getBuyFees().equals(pricing.get().getBuyFees()))
            pricing.get().setBuyFees(dto.getBuyFees());
        if(dto.getBuyMax() != null && !dto.getBuyMax().equals(pricing.get().getBuyMax()))
            pricing.get().setBuyMax(dto.getBuyMax());
        if(dto.getBuyMin() != null && !dto.getBuyMin().equals(pricing.get().getBuyMin()))
            pricing.get().setBuyMin(dto.getBuyMin());

        if(dto.getDepositMax() != null && !dto.getDepositMax().equals(pricing.get().getDepositMax()))
            pricing.get().setDepositMax(dto.getDepositMax());
        if(dto.getDepositMin() != null && !dto.getDepositMin().equals(pricing.get().getDepositMin()))
            pricing.get().setDepositMin(dto.getDepositMin());

        if(dto.getWithdrawalFees() != null && !dto.getWithdrawalFees().equals(pricing.get().getWithdrawalFees()))
            pricing.get().setWithdrawalFees(dto.getWithdrawalFees());
        if(dto.getWithdrawalMax() != null && !dto.getWithdrawalMax().equals(pricing.get().getWithdrawalMax()))
            pricing.get().setWithdrawalMax(dto.getWithdrawalMax());
        if(dto.getWithdrawalMin() != null && !dto.getWithdrawalMin().equals(pricing.get().getWithdrawalMin()))
            pricing.get().setWithdrawalMin(dto.getWithdrawalMin());

        if(dto.getSellFees() != null && !dto.getSellFees().equals(pricing.get().getSellFees()))
            pricing.get().setSellFees(dto.getSellFees());
        if(dto.getSellMax() != null && !dto.getSellMax().equals(pricing.get().getSellMax()))
            pricing.get().setSellMax(dto.getSellMax());
        if(dto.getSellMin() != null && !dto.getSellMin().equals(pricing.get().getSellMin()))
            pricing.get().setSellMin(dto.getSellMin());

        if(dto.getExchangeFees() != null && !dto.getExchangeFees().equals(pricing.get().getExchangeFees()))
            pricing.get().setExchangeFees(dto.getExchangeFees());
        if(dto.getExchangeMax() != null && !dto.getExchangeMax().equals(pricing.get().getExchangeMax()))
            pricing.get().setExchangeMax(dto.getExchangeMax());
        if(dto.getExchangeMin() != null && !dto.getExchangeMin().equals(pricing.get().getExchangeMin()))
            pricing.get().setExchangeMin(dto.getExchangeMin());

        return ResponseEntity.ok(pricingRepository.save(pricing.get()));
    }

    @Override
    public ResponseEntity<List<Pricing>> getAllPricingForLevel(Long levelId) {
        // input checking
        if(levelId == null) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> getAllPricingForLevel :: PricingServiceImpl.java ");
            return ResponseEntity.badRequest().body(null);
        }

        // we now get the corresponding level
        Optional<Level> level = levelRepository.findById(levelId);
        if(!level.isPresent()) {
            logger.error("[" + new Date() + "] => LEVEL NOT FOUND >>>>>>>> getAllPricingForLevel :: PricingServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(pricingRepository.findByLevel(level.get()));
    }

    @Override
    public ResponseEntity<List<Pricing>> getAllPricingForCurrency(Long currencyId) {
        // input checking
        if(currencyId == null) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> getAllPricingForCurrency :: PricingServiceImpl.java ");
            return ResponseEntity.badRequest().body(null);
        }

        // we now get the corresponding currency
        Optional<Currency> currency = currencyRepository.findById(currencyId);
        if(!currency.isPresent()) {
            logger.error("[" + new Date() + "] => CURRENCY NOT FOUND >>>>>>>> getAllPricingForCurrency :: PricingServiceImpl.java");
            return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(pricingRepository.findByCurrency(currency.get()));
    }

    @Override
    public ResponseEntity<List<Pricing>> getAllPricing() {
        return ResponseEntity.ok(pricingRepository.findAll());
    }


    private Boolean isPricingPositive(Pricing pricing) {
        return Double.parseDouble(pricing.getBuyFees()) >= 0 && Double.parseDouble(pricing.getBuyMin()) >= 0 &&
               Double.parseDouble(pricing.getBuyMax()) >= 0 && Double.parseDouble(pricing.getDepositMin()) >= 0 &&
               Double.parseDouble(pricing.getDepositMax()) >= 0 && Double.parseDouble(pricing.getWithdrawalFees()) >= 0 &&
               Double.parseDouble(pricing.getWithdrawalMax()) >= 0 && Double.parseDouble(pricing.getWithdrawalMin()) >= 0 &&
               Double.parseDouble(pricing.getSellMax()) >= 0 && Double.parseDouble(pricing.getSellFees()) >= 0 &&
               Double.parseDouble(pricing.getSellMin()) >= 0 && Double.parseDouble(pricing.getExchangeFees()) >= 0 &&
               Double.parseDouble(pricing.getExchangeMax()) >= 0 && Double.parseDouble(pricing.getExchangeMin()) >= 0;
    }

    @Override
    public ResponseEntity<Boolean> deletePricing(Long pricingId) {
        // input checking
        if(pricingId == null) {
            logger.error("[" + new Date() + "] => INPUT NULL pricingId >>>>>>>> deletePricing :: PricingServiceImpl.java");
            return ResponseEntity.badRequest().body(false);
        }

        pricingRepository.deleteById(pricingId);
        return ResponseEntity.ok(true);
    }
}
