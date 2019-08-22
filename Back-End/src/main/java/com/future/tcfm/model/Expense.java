package com.future.tcfm.model;

import com.future.tcfm.model.list.UserContributedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "expense")
public class Expense {
    @Id
    private String idExpense;
    private String groupName;
    private String title;
    private String detail;
    private Double price;
    private String requester;
    private List<UserContributedList> userContributed;
    private Long createdDate;
//    private Long rejectedDate;
//    private Long approvedDate;
    private Boolean status;
    private Long lastModifiedAt;
    private String approvedOrRejectedBy;
    private Integer groupCurrentPeriod;
}
