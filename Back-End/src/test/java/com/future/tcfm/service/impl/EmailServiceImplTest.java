package com.future.tcfm.service.impl;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import com.future.tcfm.model.Dashboard;
import com.future.tcfm.model.Expense;
import com.future.tcfm.model.Group;
import com.future.tcfm.model.ReqResModel.EmailRequest;
import com.future.tcfm.model.User;
import com.future.tcfm.model.list.ExpenseContributedDetails;
import com.future.tcfm.model.list.ExpenseIdContributed;
import com.future.tcfm.repository.ExpenseRepository;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.UserRepository;
import com.future.tcfm.service.EmailService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.testng.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
public class EmailServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;


    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private EmailServiceImpl emailService;

    private User user;
    private Group group;
    private Expense expense;
    @Mock
    private ExpenseContributedDetails expenseContributedDetails;


    @Before
    public void before() {

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    public void periodicMailSenderTest() throws MessagingException {
        user = new User();
        user.setEmail("userTest@test.com");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        emailService.periodicMailSender(user.getEmail(), "July", 2019, "October", 2019);


    }

    @Test
    public void periodicMailSenderTestSameMonth() throws MessagingException {
        user = new User();
        user.setEmail("userTest@test.com");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        emailService.periodicMailSender(user.getEmail(), "July", 2019, "July", 2019);


    }
    @Test
    public void periodicMailSenderThisMonth() throws MessagingException {
        user = new User();
        user.setEmail("userTest@test.com");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        emailService.periodicMailSender(user.getEmail(), "THISMONTH", 2019, "July", 2019);


    }

    @Test
    public void periodicMailReminderSender() throws MessagingException {
        user = new User();
        user.setEmail("userTest@test.com");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        emailService.periodicMailReminderSender(user.getEmail());

    }

    @Test
    public void emailNotification() throws MessagingException {
        user = new User();
        user.setName("User Test");
        user.setEmail("userTest@test.com");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        emailService.emailNotification("Notification",user.getEmail());

    }

    @Test
    public void monthlyCashStatement() throws MessagingException {
        user = new User();
        user.setName("User Test");
        user.setEmail("userTest@test.com");
        user.setGroupName("Group Test");
        expense=new Expense();
        expense.setTitle("Title");
        expense.setIdExpense("ID_Expense");
        expenseContributedDetails=new ExpenseContributedDetails();
        List<ExpenseIdContributed> expenseIdContributeds = new ArrayList<>();
        ExpenseIdContributed e = new ExpenseIdContributed();
        e.setUsedBalance(5000D);
        e.setIdExpense("ID_Expense");
        expenseIdContributeds.add(e);
        user.setExpenseIdContributed(expenseIdContributeds);
        group=new Group();
        group.setCurrentPeriod(1);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(groupRepository.findByName(user.getGroupName())).thenReturn(group);
        when(expenseRepository.findByGroupNameLikeAndGroupCurrentPeriodAndStatus(user.getGroupName(),group.getCurrentPeriod(),true)).thenReturn(Arrays.asList(expense));

        emailService.monthlyCashStatement(user.getEmail());
    }


    @Test
    public void userResign() throws MessagingException {
        user = new User();
        user.setName("User Test");
        user.setEmail("userTest@test.com");
        user.setPeriodeTertinggal(0);
        user.setBalance(1000000D);
        expense=new Expense();
        expense.setTitle("Title");
        expense.setDetail("Detail ");
        expense.setIdExpense("ID_Expense");
        expense.setPrice(100D);
        List<ExpenseIdContributed> expenseIdContributeds = new ArrayList<>();
        ExpenseIdContributed e = new ExpenseIdContributed();
        e.setUsedBalance(5000D);
        e.setIdExpense("ID_Expense");
        expenseIdContributeds.add(e);
        expenseIdContributeds = new ArrayList<>();
        user.setExpenseIdContributed(expenseIdContributeds);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        emailService.userResign(user.getEmail());
    }
}
