package com.future.tcfm.model.ReqResModel;

import com.future.tcfm.model.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Overview {
    private List<Expense> latestExpense;
    private Double groupBalance;
    private String averagePerExpense;
    private Integer paymentPaidThisMonth;
    private String percentageTotalCashUsed;
    private Long latestJoinDate;
    private Integer totalMembers;
    private Long latestExpenseDate;
}
