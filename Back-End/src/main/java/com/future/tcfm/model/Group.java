package com.future.tcfm.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "group")
public class Group {
    @Id
    private String idGroup;
    private String name;
    private String groupAdmin;
    private Double regularPayment;
    private Long createdDate;
    private Long lastModifiedAt;
    private Long closedDate;
    private Double groupBalance;
    private Double balanceUsed;
    private String bankAccountNumber;
    private String bankAccountName;
//    private Double totalExpense;
    private Boolean active;
    private Integer currentPeriod;
//    @JsonIgnore // hitung selisih bulan dari tangal bulan di buat dgn tgl skrg
//    public Integer getCurrentPeriod(){
//        long selisihBulanDalamMs = System.currentTimeMillis()-getCreatedDate();
//        int selisihBulan = (int)(selisihBulanDalamMs/2.628e+9)+1;
//        return  selisihBulan;
//    }
//    @JsonIgnore
//    public Integer getCurrentPeriod(){
//        int monthCreated = Instant.ofEpochMilli(getCreatedDate()).atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue();
//        return LocalDate.now().getMonthValue()-monthCreated;
//    }

}
