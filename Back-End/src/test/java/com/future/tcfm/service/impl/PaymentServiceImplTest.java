package com.future.tcfm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.tcfm.config.SecurityConfig;
import com.future.tcfm.model.*;
import com.future.tcfm.model.ReqResModel.ExpenseRequest;
import com.future.tcfm.repository.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceImplTest {
    private static final String USER ="{\"name\":\"Test User\",\"email\":\"userTest@jyp.com\"}";
    private static final String PAYMENT="{\"idPayment\":\"IDDUMMYTEST\",\"groupName\":\"groupTest\",\"email\":\"userTest@jyp.com\"}";
    private static final String ID="IDDUMMYTEST";

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUserDetailsRepository jwtUserDetailsRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private Payment payment;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private NotificationServiceImpl notificationService;

    @Mock
    private EmailServiceImpl emailService;

    private Pattern pattern;
    private User user;
    private Group group;
    private JwtUserDetails jwtUserDetails;
    private ExpenseRequest expenseRequest;
    private MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);

        user=new User();
        user.setIdUser(ID);
        user.setGroupName("groupTest");
        group=new Group();
        group.setName("groupTest");
        jwtUserDetails=new JwtUserDetails();
        jwtUserDetails.setEmail("userTest@jyp.com");
        jwtUserDetails.setGroupName("groupTest");
    }

    @Test
    public void testUpdate() throws IOException {

        when(paymentRepository.findByIdPayment(payment.getIdPayment())).thenReturn(payment);

        ResponseEntity<?> result = paymentService.updatePayment(payment.getIdPayment(),PAYMENT,file);

        verify(paymentRepository,Mockito.times(1)).save(payment);
        Assert.assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void managementPaymentUser() throws MessagingException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new JwtUserDetails(), null));


        group = new Group();
        group.setCurrentPeriod(3);
        group.setGroupBalance(100D);

        expenseRequest=new ExpenseRequest();
        expenseRequest.setStatus(true);
        expenseRequest.setId("ID");

        user = new User();
        user.setEmail("test@test.com");
        user.setBalance(10000D);
        user.setPeriodeTertinggal(-1);
        user.setTotalPeriodPayed(1);

        payment=new Payment();
        payment.setEmailMemberLain("test@test.com");
        payment.setIsChecked(false);
        payment.setPrice(5000D);
        payment.setPeriode(2);

        when(groupRepository.findByName(user.getGroupName())).thenReturn(group);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(paymentRepository.findByIdPayment(expenseRequest.getId())).thenReturn(payment);

        paymentService.managementPayment(expenseRequest);
    }

    @Test
    public void managementPaymentUserNotFound() throws MessagingException {
        expenseRequest=new ExpenseRequest();
        expenseRequest.setStatus(true);
        expenseRequest.setId("ID");

        payment=new Payment();
        payment.setEmailMemberLain("email");

        when(paymentRepository.findByIdPayment(expenseRequest.getId())).thenReturn(payment);

        paymentService.managementPayment(expenseRequest);
    }




    @Test
    public void managementPaymentNull() throws MessagingException {
        expenseRequest=new ExpenseRequest();
        expenseRequest.setStatus(true);
        expenseRequest.setId("ID");

        payment=new Payment();
        payment.setEmailMemberLain("another@test.com");

        paymentService.managementPayment(expenseRequest);
    }

    @Test
    public void testCreate() throws IOException, MessagingException {
        //to provide security config
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new JwtUserDetails(), null));


        doReturn(user).when(userRepository).findByEmail("userTest@jyp.com");
        doReturn(group).when(groupRepository).findByName(user.getGroupName());
        when(paymentRepository.save(payment)).thenReturn(payment);
        String jsonString = PAYMENT;
        ResponseEntity createdPayment = paymentService.createPayment(jsonString,file);

        when(paymentRepository.count()).thenReturn(1L);

        Assert.assertNotNull("Created payment must not be null", createdPayment);

        Payment payment  = new ObjectMapper().readValue(jsonString, Payment.class);
        Assert.assertEquals(ID, payment.getIdPayment());
        Assert.assertEquals("groupTest", payment.getGroupName());

        verify(paymentRepository, times(1)).save(payment);
        verify(paymentRepository, times(1)).count();
    }

    @Test
    public void testCreate2() throws IOException, MessagingException {
        //to provide security config
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new JwtUserDetails(), null));


        doReturn(user).when(userRepository).findByEmail("userTest@jyp.com");
        doReturn(group).when(groupRepository).findByName(user.getGroupName());
        when(paymentRepository.save(payment)).thenReturn(payment);
        String jsonString = PAYMENT;
        ResponseEntity createdPayment = paymentService.updatePayment(jsonString,PAYMENT,file);

        when(paymentRepository.count()).thenReturn(1L);

        Assert.assertNotNull("Created payment must not be null", createdPayment);

        Payment payment  = new ObjectMapper().readValue(jsonString, Payment.class);
        Assert.assertEquals(ID, payment.getIdPayment());
        Assert.assertEquals("groupTest", payment.getGroupName());

        verify(paymentRepository, times(1)).save(payment);
        verify(paymentRepository, times(1)).count();
    }

    @Test
    public void testFindAllDataExists() throws IOException, MessagingException {
        when(paymentRepository.save(payment)).thenReturn(payment);
        paymentService.createPayment(PAYMENT,file);
        Payment paymentTest = new Payment();
        paymentTest.setIdPayment(ID);
        when(paymentRepository.save(paymentTest)).thenReturn(paymentTest);
        paymentService.createPayment(PAYMENT,file);

        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment, paymentTest));
        ResponseEntity<?> result = paymentService.findAll();
        Assert.assertEquals(result.getStatusCode(), HttpStatus.OK);
        verify(paymentRepository, times(1)).save(payment);
        verify(paymentRepository, times(1)).save(paymentTest);
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    public void searchByTest(){
        String query = "";
        int page = 1;
        int size = 2;

        Page<Payment> payments = paymentService.searchBy(query,page,size);

        verify(paymentRepository,Mockito.times(1)).save(payment);
        Assert.assertEquals(payments.getTotalPages(), 2);
    }

    @Test
    public void findById(){
        String id="ID";
        paymentService.findById(id);
    }
}
