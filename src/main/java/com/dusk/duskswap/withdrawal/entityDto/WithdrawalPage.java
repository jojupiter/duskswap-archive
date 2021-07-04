package com.dusk.duskswap.withdrawal.entityDto;

import com.dusk.duskswap.withdrawal.models.Withdrawal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WithdrawalPage {
    private Long totalItems;
    private Integer totalNumberPages;
    private Integer currentPage;
    private List<Withdrawal> withdrawalList;
}
