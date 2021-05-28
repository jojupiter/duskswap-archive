package com.dusk.shared.newslettersManagement.services;


import com.dusk.shared.newslettersManagement.models.NewsLetterSubScriber;

import java.util.List;

public interface NewsLetterService {
    List<NewsLetterSubScriber> getAllNewsLetterSubscribers();
    Boolean doesSubscriberExists(String email);
    NewsLetterSubScriber addSubscriberToNewsLetter(String email);
}
