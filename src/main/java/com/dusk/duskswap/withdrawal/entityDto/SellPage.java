package com.dusk.duskswap.withdrawal.entityDto;

import com.dusk.duskswap.withdrawal.models.Sell;
import lombok.Data;

import java.util.List;

@Data
public class SellPage {
    private Long totalItems;
    private Integer totalNumberPages;
    private Integer currentPage;
    private List<Sell> sells;
}
