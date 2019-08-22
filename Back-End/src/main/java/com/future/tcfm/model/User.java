package com.future.tcfm.model;

import com.future.tcfm.model.list.ExpenseIdContributed;
import com.future.tcfm.model.list.PaymentDetail;
//import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user")
public class User {
    @Id
    private String idUser;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String role;
    private Long joinDate;
    private Long leaveDate;
    private Boolean active;
    private String groupName;
    private Double balance; //saldo user pada group yang akan dikembalikan jika user resign/pindah grup / totalregularpayment - kontribusi dalam setiap pengeluaran grup dalam bentuk uang
    private Double balanceUsed;
    private Integer periodeTertinggal; //periode tertingal, jika (-) bearti user bayar surplus melebihi periode grup skrg
    private Integer totalPeriodPayed; //save total period payed by user in number
//    private List<PaymentDetail> periodPayed;
    private String imagePath;
    private String imageURL;
    private List <ExpenseIdContributed> expenseIdContributed;
//    public void setPeriodPayed(List<PaymentDetail> periodPayed) {
//        this.periodPayed = periodPayed;
//    }
}
