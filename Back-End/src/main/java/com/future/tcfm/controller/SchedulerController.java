package com.future.tcfm.controller;

import com.future.tcfm.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
@CrossOrigin("**")
@RestController
@RequestMapping("api/scheduler")
public class SchedulerController {

    @Autowired
    SchedulerService schedulerService;

    @GetMapping("/movetonextmonth")
    public void scheduler() throws MessagingException {
        schedulerService.scheduler();
    }
    @GetMapping("/testSchedulerReminder/")
    public void schedulerReminder() throws MessagingException {
        schedulerService.schedulerReminder();
    }
    @GetMapping("/testMonthlyCashStatement")
    public void monthlyCashStatement() throws MessagingException {
        schedulerService.monthlyCashStatement();
    }
}

