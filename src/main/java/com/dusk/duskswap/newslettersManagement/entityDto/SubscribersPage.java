package com.dusk.duskswap.newslettersManagement.entityDto;

import com.dusk.duskswap.newslettersManagement.models.NewsLetterSubScriber;
import lombok.Data;

import java.util.List;

@Data
public class SubscribersPage {
    private Long totalItems;
    private Integer totalNumberPages;
    private Integer currentPage;
    private List<NewsLetterSubScriber> subScriberList;
}
