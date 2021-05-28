package com.dusk.shared.newslettersManagement.controllers;

import com.dusk.shared.newslettersManagement.models.NewsLetterSubScriber;
import com.dusk.shared.newslettersManagement.services.NewsLetterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/newsletters")
public class NewsLetterController {
    @Autowired
    private NewsLetterService newsLetterService;

    @PostMapping(value = "/add-subscriber", produces = "application/json")
    public NewsLetterSubScriber addSubscriberToNewsLetter(String email) {
        return newsLetterService.addSubscriberToNewsLetter(email);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/all-subscribers", produces = "application/json")
    public List<NewsLetterSubScriber> getAllNewsLetterSubscribers() {
        return newsLetterService.getAllNewsLetterSubscribers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/subscriber-exists", produces = "application/json")
    public Boolean doesSubscriberExist(@Param("email") String email) {
        return newsLetterService.doesSubscriberExists(email);
    }

}
