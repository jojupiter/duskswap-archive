package com.dusk.duskswap.transferring.services;

import com.dusk.duskswap.account.models.AmountCurrency;
import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.AmountCurrencyRepository;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Pricing;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.commons.repositories.PricingRepository;
import com.dusk.duskswap.commons.repositories.StatusRepository;
import com.dusk.duskswap.transferring.entityDtos.TransferPage;
import com.dusk.duskswap.transferring.models.Transfer;
import com.dusk.duskswap.transferring.repositories.TransferRepository;
import com.dusk.duskswap.usersManagement.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class TransferServiceImpl implements TransferService {

    @Autowired
    private TransferRepository transferRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private PricingRepository pricingRepository;

    @Override
    public ResponseEntity<TransferPage> getAllTransfers(Integer currentPage, Integer pageSize) {
        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Transfer> transfers = transferRepository.findAll(pageable);
        if(transfers.hasContent()) {
            TransferPage transferPage = new TransferPage();
            transferPage.setCurrentPage(transfers.getNumber());
            transferPage.setTotalItems(transfers.getTotalElements());
            transferPage.setTotalNumberPages(transfers.getTotalPages());
            transferPage.setTransfers(transfers.getContent());

            return ResponseEntity.ok(transferPage);
        }

        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<TransferPage> getAllUserTransfers(ExchangeAccount userAccount, Integer currentPage, Integer pageSize) {
        // input checking
        if(userAccount == null) {
            log.error("[" + new Date() + "] => INPUT NULL >>>>>>>> getAllUserTransfers :: TransferServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Transfer> transfers = transferRepository.findByFromAccount(userAccount, pageable);
        if(transfers.hasContent()) {
            TransferPage transferPage = new TransferPage();
            transferPage.setCurrentPage(transfers.getNumber());
            transferPage.setTotalItems(transfers.getTotalElements());
            transferPage.setTotalNumberPages(transfers.getTotalPages());
            transferPage.setTransfers(transfers.getContent());

            return ResponseEntity.ok(transferPage);
        }

        return ResponseEntity.ok(null);
    }

    @Transactional
    @Override
    public Transfer createTransfer(User sender, User recipient, ExchangeAccount fromAccount, ExchangeAccount toAccount, Long currencyId, String amount) throws Exception {
        // input checking
        if(
                fromAccount == null ||
                toAccount == null ||
                currencyId == null ||
                amount == null || (amount != null && amount.isEmpty()) || (amount != null && !amount.isEmpty() && Double.parseDouble(amount) <= 0)
        ) {
            log.error("[" + new Date() + "] => INPUT INCORRECT >>>>>>>> createTransfer :: TransferServiceImpl.java " +
                    "====== fromAccount = " + fromAccount + ", toAccount = " + toAccount + ", currencyId = " + currencyId +", amount = " + amount);
            return null;
        }

        if(sender.getLevel() == null) {
            log.error("[" + new Date() + "] => USER'S LEVEL NOT PRESENT >>>>>>>> createTransfer :: TransferServiceImpl.java");
            return null;
        }

        // ================================= Getting necessary elements =================================
        // >>>>> 1. we get the currency
        Optional<Currency> currency = currencyRepository.findById(currencyId);
        if(!currency.isPresent()) {
            log.error("[" + new Date() + "] => CURRENCY NOT PRESENT >>>>>>>> createTransfer :: TransferServiceImpl.java");
            return null;
        }
        // >>>>> 2. we get the transfer pricing
        Optional<Pricing> pricing = pricingRepository.findByLevelAndCurrency(sender.getLevel(), currency.get());
        if(!pricing.isPresent()) {
            log.error("[" + new Date() + "] => PRICING NOT PRESENT >>>>>>>> createTransfer :: TransferServiceImpl.java");
            return null;
        }
        // >>>>> 4. we check if the transfer is possible according to the pricing and the user's balance
        if(
                Double.parseDouble(amount) > Double.parseDouble(pricing.get().getTransferMax()) ||
                Double.parseDouble(amount) < Double.parseDouble(pricing.get().getTransferMin())
        ) {
            log.error("[" + new Date() + "] => AMOUNT OUT OF BOUNDS [" + pricing.get().getTransferMin()+ "," + pricing.get().getTransferMax() +" ]" +
                    " WITH amount = " + amount + " >>>>>>>> createTransfer :: TransferServiceImpl.java");
            return null;
        }

        // =============================== fees calculation + creation of the transfer ======================
        // >>>>> 4. Fees calculation
        Double duskFees = 0.0;
        if(pricing.get().getTypeTransfer().equals(DefaultProperties.PRICING_TYPE_PERCENTAGE)) {
            duskFees = Double.parseDouble(pricing.get().getTransferFees()) *
                       Double.parseDouble(amount);
        }
        else if(pricing.get().getTypeTransfer().equals(DefaultProperties.PRICING_TYPE_FIX))
            duskFees = Double.parseDouble(pricing.get().getTransferFees());

        // >>>>> 5. Transfer creation
        Transfer transfer = new Transfer();
        transfer.setAmount(amount);
        transfer.setCurrency(currency.get());
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        transfer.setFees(Double.toString(duskFees));
        transfer.setRecipientName(recipient.getFirstName() + " " + recipient.getLastName());
        transfer.setRecipientEmail(recipient.getEmail());

        return transfer;

    }

    @Override
    public Transfer saveTransfer(Transfer transfer) {
        if(transfer == null) {
            log.error("[" + new Date() + "] => INPUT INCORRECT >>>>>>>> saveTransfer :: TransferServiceImpl.java");
            return null;
        }
        return transferRepository.save(transfer);
    }
}
