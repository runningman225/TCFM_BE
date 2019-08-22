package com.future.tcfm.controller;

import com.future.tcfm.model.Expense;
import com.future.tcfm.model.ReqResModel.ExpenseRequest;
import com.future.tcfm.service.ExpenseService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.text.ParseException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@NoArgsConstructor
@RequestMapping("/api/expense")
public class ExpenseController {

    @Autowired
    ExpenseService expenseService;

    @GetMapping(value = "/last",produces = MediaType.APPLICATION_JSON_VALUE)
    public Expense getLastExpense(){
        return expenseService.getLastExpense();
    }
    @GetMapping
    public List<Expense> loadAll (){
        return expenseService.loadAll();
    }

    @GetMapping("/search")
    public Page<Expense> searchExpense(
            @RequestParam(value = "query",required = false,defaultValue="") String query,
//            @RequestParam(value = "value",required = false,defaultValue="")String value,
            @RequestParam(value = "page",required = false, defaultValue = "0") int page,
            @RequestParam(value = "size",required = false, defaultValue = "10") int size) throws ParseException {
        return expenseService.searchBy(query,page,size);
    }

    /**
     * paging
     * @param userEmail
     * @return
     */
    @GetMapping("/group")
    public Page<Expense> expenseGroup (
            @RequestParam("email") String userEmail,
            @RequestParam(value = "filter",defaultValue = "createdDate") String filter,
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size",defaultValue = "10") int size){
        return expenseService.expensePageGroupByEmail(userEmail,filter,page,size);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Expense> singleExpense(@PathVariable("id") String id) {
        return expenseService.singleExpense(id);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createExpense(@RequestBody Expense expense) throws MessagingException {
        return expenseService.createExpense(expense);
    }

    @PutMapping("/managementExpense")
    public ResponseEntity managementExpense(@RequestBody ExpenseRequest expenseRequest) throws MessagingException {
        return expenseService.managementExpense(expenseRequest);
    }
}


