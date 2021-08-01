package com.dusk.duskswap.exchange.entityDto;

import com.dusk.duskswap.exchange.models.Exchange;
import lombok.Data;

import java.util.List;

@Data
public class ExchangePage {
    private Long totalItems;
    private Integer totalNumberPages;
    private Integer currentPage;
    private List<Exchange> exchanges;
}
