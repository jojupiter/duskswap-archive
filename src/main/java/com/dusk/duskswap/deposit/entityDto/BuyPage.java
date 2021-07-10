package com.dusk.duskswap.deposit.entityDto;

import com.dusk.duskswap.deposit.models.Buy;
import lombok.Data;

import java.util.List;

@Data
public class BuyPage {
    private Long totalItems;
    private Integer totalNumberPages;
    private Integer currentPage;
    private List<Buy> buyList;
}
