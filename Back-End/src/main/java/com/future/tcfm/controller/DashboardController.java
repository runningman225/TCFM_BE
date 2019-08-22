package com.future.tcfm.controller;

import com.future.tcfm.model.Dashboard;
import com.future.tcfm.model.Expense;
import com.future.tcfm.service.DashboardService;
import com.future.tcfm.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Autowired
    DashboardService dashboardService;

    @GetMapping
    public Dashboard getData(@RequestParam("email") String email) {
        return dashboardService.getData(email);
    }


}