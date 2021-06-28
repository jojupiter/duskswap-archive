package com.dusk.duskswap.newslettersManagement.repositories;

import com.dusk.duskswap.newslettersManagement.models.NewsLetterSubScriber;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NewsLetterSubscriberRepository extends CrudRepository<NewsLetterSubScriber, Long> {
    List<NewsLetterSubScriber> findAll();
    Boolean existsByEmail(String email);
}
