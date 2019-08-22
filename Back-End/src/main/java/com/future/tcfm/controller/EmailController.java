package com.future.tcfm.controller;

import com.future.tcfm.model.ReqResModel.EmailRequest;
import com.future.tcfm.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.mail.MessagingException;
@RequestMapping("/email")
@Controller
public class EmailController {

    @Autowired
    EmailService emailService;

    @PostMapping("/userResign")
    public ResponseEntity userResign(@RequestBody String email) throws MessagingException {
        return emailService.userResign(email);
    }

}
