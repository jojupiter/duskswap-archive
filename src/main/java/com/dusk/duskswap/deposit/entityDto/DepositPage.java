package com.dusk.duskswap.deposit.entityDto;

import com.dusk.duskswap.deposit.models.Deposit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DepositPage {
    private Long totalItems;
    private Integer totalNumberPages;
    private Integer currentPage;
    private List<Deposit> deposits;
}
