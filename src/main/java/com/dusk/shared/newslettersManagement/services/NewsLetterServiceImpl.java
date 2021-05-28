package com.dusk.shared.newslettersManagement.services;

import com.dusk.shared.commons.mailing.models.Email;
import com.dusk.shared.commons.mailing.services.EmailService;
import com.dusk.shared.newslettersManagement.models.NewsLetterSubScriber;
import com.dusk.shared.newslettersManagement.repositories.NewsLetterSubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsLetterServiceImpl implements NewsLetterService {

    @Autowired
    private NewsLetterSubscriberRepository newsLetterSubscriberRepository;
    @Autowired
    private EmailService emailService;

    @Override
    public List<NewsLetterSubScriber> getAllNewsLetterSubscribers() {
        return newsLetterSubscriberRepository.findAll();
    }

    @Override
    public Boolean doesSubscriberExists(String email) {
        if(email.isEmpty() || email == null)
            return null;
        return newsLetterSubscriberRepository.existsByEmail(email);
    }

    @Override
    public NewsLetterSubScriber addSubscriberToNewsLetter(String email) {
        if(email.isEmpty() || email == null)
            return null;
        NewsLetterSubScriber newsLetterSubScriber = new NewsLetterSubScriber();
        newsLetterSubScriber.setAccept(true);
        newsLetterSubScriber.setEmail(email);

        // notifying the user by mail
        Email notificationMail = new Email();


        return newsLetterSubscriberRepository.save(newsLetterSubScriber);
    }
}
