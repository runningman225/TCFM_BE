package com.future.tcfm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.tcfm.model.Group;
import com.future.tcfm.model.Payment;
import com.future.tcfm.model.ReqResModel.ExpenseRequest;
import com.future.tcfm.model.User;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.PaymentRepository;
import com.future.tcfm.repository.UserRepository;
import com.future.tcfm.service.EmailService;
import com.future.tcfm.service.NotificationService;
import com.future.tcfm.service.PaymentService;

import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.xml.transform.OutputKeys;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.future.tcfm.config.SecurityConfig.getCurrentUser;
import static com.future.tcfm.service.impl.ExpenseServiceImpl.createPageRequest;
import static com.future.tcfm.service.impl.NotificationServiceImpl.*;
import static com.future.tcfm.service.impl.UserServiceImpl.*;

@Service
public class PaymentServiceImpl implements PaymentService {
    public static final String UPLOADED_FOLDER="../assets/user/";
    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    NotificationService notificationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    MongoTemplate mongoTemplate;

    private ExecutorService executor = Executors.newSingleThreadExecutor();


    String notificationMessage;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository=paymentRepository;
    }

    @Override
    public ResponseEntity createPayment(String paymentJSONString, MultipartFile file) throws IOException, MessagingException {
        Payment payment  = new ObjectMapper().readValue(paymentJSONString, Payment.class);
        System.out.print("Isi payment:");
        System.out.println(payment);
        User userExist = userRepository.findByEmail(payment.getEmail());
        if(userExist==null){
            return new ResponseEntity("User email does not exist", HttpStatus.NOT_FOUND);
        }
        Group groupExist = groupRepository.findByName(userExist.getGroupName());

        if(groupExist==null) {
            return new ResponseEntity("Group name does not exist", HttpStatus.NOT_FOUND);
        }
        if(payment.getEmail() == null){
            return new ResponseEntity("400: Payment is null", HttpStatus.BAD_REQUEST);
        }
        if(checkImageFile(file)){
            try {
                String fileName = "payment/"+System.currentTimeMillis() + "_" + payment.getEmail() + "_" + file.getOriginalFilename();
                saveUploadedFile(file,fileName);
                payment.setImagePath(fileName);
                payment.setImageURL(UPLOADED_URL+fileName);
            } catch (IOException e){
                return new ResponseEntity<>("Some error occurred. Failed to add image", HttpStatus.BAD_REQUEST);
            }
        }
        payment.setIsChecked(false);
        payment.setPaymentDate(payment.getPaymentDate()==null ? System.currentTimeMillis() : payment.getPaymentDate());
        payment.setGroupName(userExist.getGroupName());
        payment.setLastModifiedAt(System.currentTimeMillis());
        paymentRepository.save(payment);
        notificationMessage = payment.getEmail()+ PAYMENT_MESSAGE; //getCurrentUser() = get current logged in user
        notificationService.createNotification(notificationMessage,getCurrentUser().getEmail(),getCurrentUser().getGroupName(),TYPE_PERSONAL);
        executor.execute(() -> {
            try {
                emailService.emailNotification(notificationMessage, getCurrentUser().getEmail());//pengiriman email untuk user yang berkontribusi
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
        return new ResponseEntity<>("Succeed to create payment!",HttpStatus.OK);

    }

    @Override
    public ResponseEntity updatePayment(String id, String paymentJSONString, MultipartFile file) throws IOException {
        Payment payment = new ObjectMapper().readValue(paymentJSONString, Payment.class);
        Payment paymentExist = paymentRepository.findByIdPayment(id);
        if (paymentExist == null) {
            return new ResponseEntity("Payment not found!", HttpStatus.NOT_FOUND);
        }
        if (checkImageFile(file)) {
            try {
                if (paymentExist.getImagePath() != null) {
                    Path deletePath = Paths.get(UPLOADED_FOLDER + paymentExist.getImagePath());
                    Files.delete(deletePath);
                }
                String fileName="payment/"+String.valueOf(System.currentTimeMillis())+"_"+payment.getEmail()+"_"+file.getOriginalFilename();
                saveUploadedFile(file, fileName);
                paymentExist.setImagePath(fileName);
                paymentExist.setImageURL(UPLOADED_URL + fileName);
            } catch (IOException e) {
                return new ResponseEntity<>("Some error occured. Failed to add image", HttpStatus.BAD_REQUEST);
            }
        }
        paymentExist.setIsRejected(null);
        paymentExist.setPrice(payment.getPrice());
        paymentExist.setLastModifiedAt(System.currentTimeMillis());
        paymentExist.setPeriode(payment.getPeriode());

        paymentRepository.save(payment);
        return new ResponseEntity(paymentExist, HttpStatus.OK);
    }


    @Override
    public ResponseEntity managementPayment(ExpenseRequest thisPayment) throws MessagingException {
        Payment paymentExist = paymentRepository.findByIdPayment(thisPayment.getId());

        if(paymentExist==null){
            return new ResponseEntity("Payment not found!",HttpStatus.NOT_FOUND);
        }
        String payFor=paymentExist.getEmailMemberLain().equals("")?paymentExist.getEmail():paymentExist.getEmailMemberLain();
        User payForExist=userRepository.findByEmail(payFor);
        if(payForExist==null){
            return new ResponseEntity("User not found!",HttpStatus.NOT_FOUND);
        }

        if(thisPayment.getStatus()){
            if(!paymentExist.getIsChecked()){
                paymentExist.setIsRejected(false);
                Group group = groupRepository.findByName(paymentExist.getGroupName());
                if(payForExist.getBalance()==null){
                    payForExist.setBalance(0.0);
                }
                payForExist.setBalance(payForExist.getBalance()+paymentExist.getPrice());
                payForExist.setTotalPeriodPayed(payForExist.getTotalPeriodPayed()+paymentExist.getPeriode());
                payForExist.setPeriodeTertinggal(group.getCurrentPeriod()-payForExist.getTotalPeriodPayed());// jika minus bearti user surplus
                userRepository.save(payForExist);

                group.setGroupBalance(group.getGroupBalance()+paymentExist.getPrice());
                groupRepository.save(group);

                notificationMessage = paymentExist.getEmail()+ PAYMENT_APPROVED_MESSAGE + getCurrentUser().getEmail(); //getCurrentUser() = get current logged in user
            }
        }
        else {
            paymentExist.setIsRejected(true);
            notificationMessage = paymentExist.getEmail()+ PAYMENT_REJECTED_MESSAGE + getCurrentUser().getEmail(); //getCurrentUser() = get current logged in user
        }
        paymentExist.setIsChecked(true);
        paymentExist.setLastModifiedAt(System.currentTimeMillis());
        paymentRepository.save(paymentExist);
        notificationService.createNotification(notificationMessage,paymentExist.getEmail(),null,TYPE_PERSONAL);
        emailService.emailNotification(notificationMessage,paymentExist.getEmail());
        executor.execute(() -> {
            try {
                emailService.emailNotification(notificationMessage, paymentExist.getEmail());//pengiriman email untuk user yang berkontribusi
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
        return new ResponseEntity(paymentExist,HttpStatus.OK);
    }


    @Override
    public ResponseEntity findAll() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }

    @Override
    public ResponseEntity findById(String id) {
        Payment paymentExist = paymentRepository.findByIdPayment(id);
        if(paymentExist==null){
            return new ResponseEntity("\"\\\"{\\\"error\\\":\\\"404 not found\\\"\"",HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(paymentExist,HttpStatus.OK);

    }

    @Override
    public ResponseEntity findByEmail(String email,String filter,int page, int size) {
        Page<Payment> paymentExist = paymentRepository.findAllByEmailOrderByLastModifiedAt(email,createPageRequest(filter,"desc",page,size));
        if(paymentExist==null){
            return new ResponseEntity("\"\\\"{\\\"error\\\":\\\"404 not found\\\"\"",HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(paymentExist,HttpStatus.OK);

    }

    @Override
    public Page<Payment> searchBy(String query, int page, int size) {
        System.out.println("Query Param : "+query);
        Pattern pattern = Pattern.compile("(.*)(:)(.*)");
        Matcher matcher = pattern.matcher(query);
        if(!matcher.find()){return null;}
        String key=matcher.group(1);
        String value=matcher.group(3);
        System.out.println("Key : "+key+"; Value : "+value);
        Pageable pageable = createPageRequest("paymentDate","desc",page,size);
        Query myQuery = new Query().with(pageable);
        System.out.println("Authorities : "+getCurrentUser().getAuthorities());
        String myRole = getCurrentUser().getAuthorities().toString();
        String groupName = myRole.contains("SUPER_ADMIN") ? "" : getCurrentUser().getGroupName();
        Criteria criteria = Criteria.where(key).regex(value,"i").and("groupName").regex(groupName);
        if(key.equalsIgnoreCase("date before")){
            key = "paymentDate";
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
            long timeStamp= System.currentTimeMillis();
            try {
                timeStamp = formatter.parse(value).getTime();
            }catch (Exception e){e.printStackTrace();}
            criteria = Criteria.where(key).lte(timeStamp).and("groupName").regex(groupName);
        }else if(key.equalsIgnoreCase("date after")){
            key = "paymentDate";
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
            long timeStamp= 0;
            try {
                timeStamp = formatter.parse(value).getTime();
            }catch (Exception e){e.printStackTrace();}
            criteria = Criteria.where(key).gte(timeStamp).and("groupName").regex(groupName);
        } else if(key.equalsIgnoreCase("status")){
            key = "isRejected";
            Boolean isRejected = !value.equalsIgnoreCase("accepted");
            isRejected = value.equalsIgnoreCase("") || value.equalsIgnoreCase("pending") ? null : isRejected;
            criteria = Criteria.where(key).is(isRejected).and("groupName").regex(groupName);
        } else if(key.equalsIgnoreCase("periode <")){
            key=key.substring(0,7);
            int dValue = value.equalsIgnoreCase("")? Integer.MAX_VALUE :Integer.parseInt(value);
            criteria = Criteria.where(key).lt(dValue).and("groupName").regex(groupName);
        } else if(key.equalsIgnoreCase("periode >")){
                key=key.substring(0,7);
            int dValue = value.equalsIgnoreCase("")? 0 :Integer.parseInt(value);
            criteria = Criteria.where(key).gt(dValue).and("groupName").regex(groupName);
        }
        myQuery.addCriteria(criteria).with(new Sort(Sort.Direction.DESC,"paymentDate"));
        List<Payment> paymentList =  mongoTemplate.find(myQuery,Payment.class,"payment");
        return PageableExecutionUtils.getPage(
                paymentList,
                pageable,
                () -> mongoTemplate.count(myQuery, Payment.class));
    }
}
