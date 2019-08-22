package com.future.tcfm.model.list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseContributedDetails {
    private String title;
    private String detail;
    private String price;

    @Override
    public String toString(){
        return String.format("Title : %-20s<br>Detail : %-20s<br>Your Contribution : %-20s<br><br>",title,detail,price);
    }
}
