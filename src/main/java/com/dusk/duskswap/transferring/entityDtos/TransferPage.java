package com.dusk.duskswap.transferring.entityDtos;


import com.dusk.duskswap.transferring.models.Transfer;
import lombok.Data;

import java.util.List;

@Data
public class TransferPage {
    private Long totalItems;
    private Integer totalNumberPages;
    private Integer currentPage;
    private List<Transfer> transfers;
}
