package com.future.tcfm.service;

import com.future.tcfm.model.Expense;
import com.future.tcfm.model.ReqResModel.ExpenseRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import javax.mail.MessagingException;
import java.text.ParseException;
import java.util.List;

public interface ExpenseService {
    List<Expense> loadAll();
    List<Expense> expenseGroup(String groupName);
    ResponseEntity createExpense(Expense expense) throws MessagingException;
    ResponseEntity singleExpense(String id);
    Page<Expense> expensePageGroupByEmail(String userEmail,String filter, int page, int size);
    ResponseEntity managementExpense(ExpenseRequest expenseRequest) throws MessagingException;
    Page<Expense> searchBy(String query, int page, int size) throws ParseException;
    Expense getLastExpense();
}