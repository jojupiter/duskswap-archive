package com.dusk.duskswap.newslettersManagement.services;


import com.dusk.duskswap.newslettersManagement.entityDto.SubscribersPage;
import com.dusk.duskswap.newslettersManagement.models.NewsLetterSubScriber;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface NewsLetterService {
    ResponseEntity<SubscribersPage> getAllNewsLetterSubscribers(Integer currentPage, Integer pageSize);
    ResponseEntity<SubscribersPage> getAllActivatedSubscribers(Integer currentPage, Integer pageSize);
    List<NewsLetterSubScriber> getAllActiveSubscribers();
    Boolean doesSubscriberExists(String email);
    NewsLetterSubScriber addSubscriberToNewsLetter(String email);
    ResponseEntity<Boolean> activateSubscriber(String email, Boolean activate); // activate = true to activate the subscriber and = false to deactivate
}
