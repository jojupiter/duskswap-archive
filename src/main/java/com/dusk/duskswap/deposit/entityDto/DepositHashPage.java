package com.dusk.duskswap.deposit.entityDto;

import com.dusk.duskswap.deposit.models.DepositHash;
import lombok.Data;

import java.util.List;

@Data
public class DepositHashPage {
    private Long totalItems;
    private Integer totalNumberPages;
    private Integer currentPage;
    private List<DepositHash> depositHashes;
}
