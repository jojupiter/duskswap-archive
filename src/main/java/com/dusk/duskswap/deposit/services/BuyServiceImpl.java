package com.dusk.duskswap.deposit.services;

import com.dusk.duskswap.account.models.ExchangeAccount;
import com.dusk.duskswap.account.repositories.ExchangeAccountRepository;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.commons.models.Currency;
import com.dusk.duskswap.commons.models.Status;
import com.dusk.duskswap.commons.models.TransactionOption;
import com.dusk.duskswap.commons.repositories.CurrencyRepository;
import com.dusk.duskswap.commons.repositories.StatusRepository;
import com.dusk.duskswap.commons.repositories.TransactionOptionRepository;
import com.dusk.duskswap.deposit.entityDto.BuyDto;
import com.dusk.duskswap.deposit.entityDto.BuyPage;
import com.dusk.duskswap.deposit.models.Buy;
import com.dusk.duskswap.deposit.repositories.BuyRepository;
import com.dusk.duskswap.usersManagement.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class BuyServiceImpl implements BuyService {

    @Autowired
    private BuyRepository buyRepository;
    @Autowired
    private ExchangeAccountRepository exchangeAccountRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private TransactionOptionRepository transactionOptionRepository;
    private Logger logger = LoggerFactory.getLogger(BuyServiceImpl.class);

    @Override
    public Buy createBuy(User user, BuyDto dto, String payToken, String notifToken) {
        // input checking
        if(dto == null) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        // here we get the corresponding exchange account and verify if exists
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        //then, we get the currency
        Optional<Currency> currency = currencyRepository.findById(dto.getToCurrencyId());
        if(!currency.isPresent()) {
            logger.error("[" + new Date() + "] => CURRENCY ACCOUNT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        // next, we get the transaction option
        Optional<TransactionOption> transactionOption = transactionOptionRepository.findById(dto.getTransactionOptId());
        if(!transactionOption.isPresent()) {
            logger.error("[" + new Date() + "] => TRANSACTION OPT NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        // Here we get the "transaction processing/in_confirmation" status to assign it to the new buy command
        Optional<Status> status = statusRepository.findByName(DefaultProperties.STATUS_TRANSACTION_IN_CONFIRMATION);
        if(!status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> createBuy :: BuyServiceImpl.java");
            return null;
        }

        // After getting the necessary elements, we create the buy command
        Buy buy = new Buy();
        buy.setNotifToken(notifToken);
        buy.setPayToken(payToken);
        //buy.setAmount(dt);
        buy.setExchangeAccount(exchangeAccount.get());
        buy.setTransactionOption(transactionOption.get());
        buy.setToCurrency(currency.get());
        buy.setStatus(status.get());

        return buyRepository.save(buy);
    }

    @Override
    public String calculateBuyAmount(Long fromCurencyId, Long toCurrency, String fromAmount) {
        // input checking
        if(
             toCurrency == null ||
             fromAmount == null || (fromAmount != null && fromAmount.isEmpty())
        ) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            return null;
        }

        return null;
    }

    @Override
    public Buy updateBuyStatus(String payToken, String statusString) {
        // input checking
        if(
                payToken == null || (payToken != null && payToken.isEmpty()) ||
                statusString == null || (statusString != null && payToken.isEmpty())
        ) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            return null;
        }

        // Now, we get the saved buy according to its pay token
        Optional<Buy> buy = buyRepository.findByPayToken(payToken);
        if(!buy.isPresent()) {
            logger.error("[" + new Date() + "] => BUY NOT PRESENT >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            return null;
        }
        // and we get the corresponding status too
        Optional<Status> status = statusRepository.findByName(statusString);
        if(!status.isPresent()) {
            logger.error("[" + new Date() + "] => STATUS NOT PRESENT >>>>>>>> updateBuyStatus :: BuyServiceImpl.java");
            return null;
        }

        // finally we proceed to the update
        buy.get().setStatus(status.get());

        return buyRepository.save(buy.get());
    }

    @Override
    public ResponseEntity<BuyPage> getAllBuy(Integer currentPage, Integer pageSize) {

        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Buy> buys = buyRepository.findAll(pageable);
        if(buys.hasContent()) {
            BuyPage buyPage = new BuyPage();
            buyPage.setCurrentPage(buys.getNumber());
            buyPage.setTotalItems(buys.getTotalElements());
            buyPage.setTotalNumberPages(buys.getTotalPages());
            buyPage.setBuyList(buys.getContent());

            return ResponseEntity.ok(buyPage);
        }

        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<BuyPage> getAllBuyByUser(User user, Integer currentPage, Integer pageSize) {

        // input checking
        if(user == null) {
            logger.error("[" + new Date() + "] => INPUT INCORRECT (null or empty) >>>>>>>> getAllBuyByUser :: BuyServiceImpl.java");
            return ResponseEntity.badRequest().body(null);
        }

        // after checking input, we get the user's account
        Optional<ExchangeAccount> exchangeAccount = exchangeAccountRepository.findByUser(user);
        if(!exchangeAccount.isPresent()) {
            logger.error("[" + new Date() + "] => EXCHANGE ACCOUNT NOT PRESENT >>>>>>>> getAllBuyByUser :: BuyServiceImpl.java");
            return null;
        }

        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Buy> buys = buyRepository.findByExchangeAccount(exchangeAccount.get(), pageable);
        if(buys.hasContent()) {
            BuyPage buyPage = new BuyPage();
            buyPage.setCurrentPage(buys.getNumber());
            buyPage.setTotalItems(buys.getTotalElements());
            buyPage.setTotalNumberPages(buys.getTotalPages());
            buyPage.setBuyList(buys.getContent());

            return ResponseEntity.ok(buyPage);
        }

        return ResponseEntity.ok(null);

    }

}
