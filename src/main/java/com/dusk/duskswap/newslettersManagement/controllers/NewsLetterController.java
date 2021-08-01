package com.dusk.duskswap.newslettersManagement.controllers;

import com.dusk.duskswap.commons.mailing.services.EmailService;
import com.dusk.duskswap.newslettersManagement.entityDto.SubscribersPage;
import com.dusk.duskswap.newslettersManagement.models.NewsLetterSubScriber;
import com.dusk.duskswap.newslettersManagement.services.NewsLetterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/newsletters")
public class NewsLetterController {
    @Autowired
    private NewsLetterService newsLetterService;
    @Autowired
    private EmailService emailService;

    @PostMapping(value = "/add-subscriber", produces = "application/json")
    public NewsLetterSubScriber addSubscriberToNewsLetter(String email) {
        return newsLetterService.addSubscriberToNewsLetter(email);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all-subscribers", produces = "application/json")
    public ResponseEntity<SubscribersPage> getAllNewsLetterSubscribers(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return newsLetterService.getAllNewsLetterSubscribers(currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all-activated-subscribers", produces = "application/json")
    public ResponseEntity<SubscribersPage> getAllActivatedSubscribers(@RequestParam(name = "currentPage", defaultValue = "0") Integer currentPage,
                                                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return newsLetterService.getAllActivatedSubscribers(currentPage, pageSize);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/subscriber-exists", produces = "application/json")
    public Boolean doesSubscriberExist(@Param("email") String email) {
        return newsLetterService.doesSubscriberExists(email);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/activate-subscriber", produces = "application/json")
    public ResponseEntity<Boolean> activateSubscriber(@Param("email") String email) {
        return newsLetterService.activateSubscriber(email, true);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/deactivate-subscriber", produces = "application/json")
    public ResponseEntity<Boolean> deactivateSubscriber(@Param("email") String email) {
        return newsLetterService.activateSubscriber(email, false);
    }

}
