package com.dusk.duskswap.newslettersManagement.repositories;

import com.dusk.duskswap.newslettersManagement.models.NewsLetterSubScriber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface NewsLetterSubscriberRepository extends PagingAndSortingRepository<NewsLetterSubScriber, Long> {
    Page<NewsLetterSubScriber> findAll(Pageable pageable);
    Optional<NewsLetterSubScriber> findByEmail(String email);
    Page<NewsLetterSubScriber> findByActivated(Boolean activated, Pageable pageable);
    List<NewsLetterSubScriber> findByActivated(Boolean activated);
    Boolean existsByEmail(String email);
}
