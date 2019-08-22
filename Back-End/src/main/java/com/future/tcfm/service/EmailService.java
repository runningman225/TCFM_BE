package com.future.tcfm.service;

import com.future.tcfm.model.ReqResModel.EmailRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

import javax.mail.MessagingException;

public interface EmailService {

    @Async
    void emailNotification(String message, String email)  throws MessagingException;

    @Async
    ResponseEntity userResign(String email) throws MessagingException;

    @Async
    void periodicMailSender (String email, String monthBeforeStr,int yearBefore, String monthNowStr, int yearNow) throws MessagingException;

    @Async
    void periodicMailReminderSender (String email) throws MessagingException;

    @Async
    void monthlyCashStatement(String email) throws MessagingException;
}
