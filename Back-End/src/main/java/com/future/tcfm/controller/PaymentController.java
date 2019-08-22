package com.future.tcfm.controller;

import com.future.tcfm.model.Payment;
import com.future.tcfm.model.ReqResModel.ExpenseRequest;
import com.future.tcfm.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;

import static com.future.tcfm.config.SecurityConfig.getCurrentUser;

@CrossOrigin
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @Autowired
    PaymentService paymentService;

//    @GetMapping
//    public ResponseEntity getPaymentByGroupName(
////            @PathVariable("groupName") String groupName,
//                                                @RequestParam(value = "filter",required = false, defaultValue = "isPaid")String filter,
//                                                @RequestParam(value = "page",required = false, defaultValue = "0")int page,
//                                                @RequestParam(value = "size",required = false, defaultValue = "10")int size) {
//        return paymentService.findByGroupNameAndIsPaid(getCurrentUser().getGroupName(),filter,page,size);
//    }

    @GetMapping("/search")
    public Page<Payment> searchPayment(
    //            @PathVariable("groupName") String groupName,
            @RequestParam(value = "query",required = false, defaultValue = "")String query,
            @RequestParam(value = "page",required = false, defaultValue = "0")int page,
            @RequestParam(value = "size",required = false, defaultValue = "10")int size) {
        return paymentService.searchBy(query,page,size);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity create(
            @Nullable @RequestPart("file") MultipartFile file,
            @RequestPart("payment") String paymentJSONString
    ) throws IOException, MessagingException {
        return paymentService.createPayment(paymentJSONString, file);
    }

    @PutMapping("/managementPayment")
    public ResponseEntity managementPayment(@RequestBody ExpenseRequest thisPayment) throws MessagingException {
        return paymentService.managementPayment(thisPayment);
    }
    @GetMapping("/{id}")
    public ResponseEntity managementPayment(@PathVariable("id") String id) {
        return paymentService.findById(id);
    }
}