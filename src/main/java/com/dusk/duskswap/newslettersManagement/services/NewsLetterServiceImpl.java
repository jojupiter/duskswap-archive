package com.dusk.duskswap.newslettersManagement.services;

import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import com.dusk.duskswap.newslettersManagement.entityDto.SubscribersPage;
import com.dusk.duskswap.newslettersManagement.models.NewsLetterSubScriber;
import com.dusk.duskswap.newslettersManagement.repositories.NewsLetterSubscriberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NewsLetterServiceImpl implements NewsLetterService {

    @Autowired
    private NewsLetterSubscriberRepository newsLetterSubscriberRepository;

    @Override
    public ResponseEntity<SubscribersPage> getAllNewsLetterSubscribers(Integer currentPage, Integer pageSize) {
        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<NewsLetterSubScriber> newsLetterSubscribers = newsLetterSubscriberRepository.findAll(pageable);
        if(newsLetterSubscribers.hasContent()) {
            SubscribersPage subscribersPage = new SubscribersPage();
            subscribersPage.setCurrentPage(newsLetterSubscribers.getNumber());
            subscribersPage.setSubScriberList(newsLetterSubscribers.getContent());
            subscribersPage.setTotalItems(newsLetterSubscribers.getTotalElements());
            subscribersPage.setTotalNumberPages(newsLetterSubscribers.getTotalPages());

            return ResponseEntity.ok(subscribersPage);
        }
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<SubscribersPage> getAllActivatedSubscribers(Integer currentPage, Integer pageSize) {
        if(currentPage == null) currentPage = 0;
        if(pageSize == null) pageSize = DefaultProperties.DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<NewsLetterSubScriber> newsLetterSubscribers = newsLetterSubscriberRepository.findByActivated(true, pageable);
        if(newsLetterSubscribers.hasContent()) {
            SubscribersPage subscribersPage = new SubscribersPage();
            subscribersPage.setCurrentPage(newsLetterSubscribers.getNumber());
            subscribersPage.setSubScriberList(newsLetterSubscribers.getContent());
            subscribersPage.setTotalItems(newsLetterSubscribers.getTotalElements());
            subscribersPage.setTotalNumberPages(newsLetterSubscribers.getTotalPages());

            return ResponseEntity.ok(subscribersPage);
        }
        return ResponseEntity.ok(null);
    }

    @Override
    public List<NewsLetterSubScriber> getAllActiveSubscribers() {
        return newsLetterSubscriberRepository.findByActivated(true);
    }

    @Override
    public Boolean doesSubscriberExists(String email) {
        if(email == null || (email != null && email.isEmpty())) {
            log.error("[" + new Date() + "] => EMAIL NULL OR EMPTY >>>>>>>> doesSubscriberExists :: NewsLetterServiceImpl.java");
            return null;
        }
        return newsLetterSubscriberRepository.existsByEmail(email);
    }

    @Override
    public NewsLetterSubScriber addSubscriberToNewsLetter(String email) {
        if(email == null && (email != null && email.isEmpty())) {
            log.error("[" + new Date() + "] => EMAIL NULL OR EMPTY >>>>>>>> addSubscriberToNewsLetter :: NewsLetterServiceImpl.java");
            return null;
        }
        if(doesSubscriberExists(email)) {
            log.info("[" + new Date() + "] => EMAIL ALREADY EXISTS >>>>>>>> addSubscriberToNewsLetter :: NewsLetterServiceImpl.java");
            return null;
        }
        NewsLetterSubScriber newsLetterSubScriber = new NewsLetterSubScriber();
        newsLetterSubScriber.setActivated(true);
        newsLetterSubScriber.setEmail(email);

        return newsLetterSubscriberRepository.save(newsLetterSubScriber);
    }

    @Override
    public ResponseEntity<Boolean> activateSubscriber(String email, Boolean activated) {
        if(
                email == null || (email != null && email.isEmpty()) ||
                activated == null
        ) {
            log.error("[" + new Date() + "] => EMAIL NULL OR EMPTY >>>>>>>> addSubscriberToNewsLetter :: NewsLetterServiceImpl.java");
            return ResponseEntity.badRequest().body(false);
        }

        Optional<NewsLetterSubScriber> newsLetterSubScriber = newsLetterSubscriberRepository.findByEmail(email);
        if(!newsLetterSubScriber.isPresent()) {
            log.error("[" + new Date() + "] => SUBSCRIBER NOT PRESENT >>>>>>>> activateSubscriber :: NewsLetterServiceImpl.java");
            return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        newsLetterSubScriber.get().setActivated(activated);
        NewsLetterSubScriber newsLetterSubScriber1 = newsLetterSubscriberRepository.save(newsLetterSubScriber.get());

        return ResponseEntity.ok(true);
    }
}
