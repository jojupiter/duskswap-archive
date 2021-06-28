package com.dusk.duskswap.newslettersManagement.services;


import com.dusk.duskswap.newslettersManagement.models.NewsLetterSubScriber;

import java.util.List;

public interface NewsLetterService {
    List<NewsLetterSubScriber> getAllNewsLetterSubscribers();
    Boolean doesSubscriberExists(String email);
    NewsLetterSubScriber addSubscriberToNewsLetter(String email);
}
