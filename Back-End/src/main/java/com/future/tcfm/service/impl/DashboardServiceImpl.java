package com.future.tcfm.service.impl;

import com.future.tcfm.model.*;
import com.future.tcfm.repository.ExpenseRepository;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.PaymentRepository;
import com.future.tcfm.repository.UserRepository;
import com.future.tcfm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final
    GroupRepository groupRepository;

    private final
    ExpenseRepository expenseRepository;

    private final
    UserRepository userRepository;

    private final
    PaymentRepository paymentRepository;

    @Autowired
    public DashboardServiceImpl(GroupRepository groupRepository, UserRepository userRepository, ExpenseRepository expenseRepository, PaymentRepository paymentRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Dashboard getData(String email) {
        User dUser = userRepository.findByEmail(email);
        Group dGroup = groupRepository.findByNameAndActive(dUser.getGroupName(),true);
        Integer totalMembers = userRepository.countByGroupName(dGroup.getName());
        List<Expense> dExpense = expenseRepository.findByGroupNameLikeAndStatusOrderByCreatedDateDesc(dUser.getGroupName(),true);

        String adminName = dGroup.getGroupAdmin();
        String accountNumber = dGroup.getBankAccountNumber();

        int monthNow= LocalDate.now().getMonthValue();
        double expenseByValue = 0;
        int expenseByQuantity = 0;
        double expenseByValueBefore = 0;
        int expenseByQuantityBefore = 0;
        int yourPayment=0;
        List<Payment> pendingPayment;
        int sumPendingPayment = 0;

        //totalExpenseByValue
        int monthExpense = 0;
        for(Expense expense:dExpense){
            LocalDate expenseDate = Instant.ofEpochMilli(expense.getLastModifiedAt()).atZone(ZoneId.systemDefault()).toLocalDate();
//            int monthExpense=expenseDate.getMonth();
            monthExpense = expenseDate.getMonthValue();
            if(monthExpense==monthNow&&expense.getStatus().equals(true)){
                expenseByValue+=expense.getPrice();
               expenseByQuantity+=1;
            }
            else if (monthExpense==monthNow-1&&expense.getStatus().equals(true)){
                expenseByValueBefore+=expense.getPrice();
                expenseByQuantityBefore+=1;
            }
        }

        //PendingPayment
        pendingPayment=paymentRepository.findByEmailAndIsChecked(dUser.getEmail(),false);
        for(Payment payment:pendingPayment){
            sumPendingPayment+=payment.getPrice();
        }

        yourPayment+=dUser.getTotalPeriodPayed();

        Dashboard d = new Dashboard();
        d.setRegularPayment(dGroup.getRegularPayment());
        d.setGroupBalance(dGroup.getGroupBalance());
        d.setTotalMembers(totalMembers);
        d.setAdminAccountNumber(accountNumber);
        d.setAdminName(adminName);
        d.setExpenseByQuantity(expenseByQuantity);
        d.setExpenseByValue(expenseByValue);
        d.setExpenseByQuantityBefore(expenseByQuantityBefore);
        d.setExpenseByValueBefore(expenseByValueBefore);
        d.setYourContribution(Double.parseDouble(new DecimalFormat("##").format(dUser.getBalanceUsed())));
        d.setYourPayment(yourPayment);
        d.setPendingPayment(sumPendingPayment);
        return d;
    }
}


//        Aggregation aggregation = Aggregation.newAggregation(
//                Aggregation.match(where("name").is(dUser.getGroupName())),
//                Aggregation.project().and("member").project("size").as("count"));
//        d.setTotalMembers(dGroup.getMember().size());

//       if(expenseByValue||ex)
//        if (expenseByValue>expenseByValueBefore)
//            expenseByValuePercent= (float) (((expenseByValue/expenseByValueBefore)-1)*100);
//        else if (expenseByValue<expenseByValueBefore)
//            expenseByValuePercent= (float) (((expenseByValueBefore/expenseByValue)-1)*(100)*-1);
//        else
//            expenseByValuePercent=0;
//
//
//        if (expenseByQuantity>expenseByQuantityBefore)
//            expenseByQuantityPercent=(((expenseByQuantity/expenseByQuantityBefore)-1)*100);
//        else if (expenseByQuantity<expenseByQuantityBefore)
//            expenseByQuantityPercent=(((expenseByQuantityBefore/expenseByQuantity)-1)*(100)*-1);
//        else
//            expenseByQuantityPercent=0;
//