package com.future.tcfm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.tcfm.model.*;
import com.future.tcfm.model.ReqResModel.ExpenseRequest;
import com.future.tcfm.model.list.ExpenseContributedDetails;
import com.future.tcfm.repository.ExpenseRepository;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.UserRepository;
import com.future.tcfm.service.NotificationService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.mail.MessagingException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ExpenseServiceImplTest {
    @Mock
    ExpenseRepository expenseRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    ExpenseServiceImpl expenseService;



    private Expense expense;
    private User user;
    private Group group;
    private ExpenseRequest expenseRequest;
    private JwtUserDetails jwtUserDetails;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateGroupExistNotNull() throws IOException, MessagingException {
        expense=new Expense();
        expense.setTitle("ALBUM");
        expense.setRequester("requesterEmail@test.com");
        expense.setPrice(5000D);
        user=new User();
        user.setGroupName("Test");
        user.setEmail("requesterEmail@test.com");
        group=new Group();
        group.setName("Test");
        group.setGroupBalance(6000D);

        when(groupRepository.findByName(user.getGroupName())).thenReturn(group);
        when(userRepository.findByEmail(expense.getRequester())).thenReturn(user);

        ResponseEntity createExpense = expenseService.createExpense(expense);
    }

    @Test
    public void testCreateGroupExistNull() throws IOException, MessagingException {
        expense=new Expense();
        expense.setTitle("ALBUM");
        expense.setRequester("requesterEmail@test.com");
        user=new User();
        user.setGroupName("Test");
        user.setEmail("requesterEmail@test.com");

        when(userRepository.findByEmail(expense.getRequester())).thenReturn(user);

        ResponseEntity createExpense = expenseService.createExpense(expense);
    }

    @Test
    public void singleExpense() throws IOException, MessagingException {
        expense=new Expense();
        expense.setIdExpense("ID_Expense");

        when(userRepository.findByEmail(expense.getRequester())).thenReturn(user);

        ResponseEntity createExpense = expenseService.singleExpense(expense.getIdExpense());
    }

    @Test
    public void singleExpenseNull() throws IOException, MessagingException {
        expense=new Expense();
        when(userRepository.findByEmail(expense.getRequester())).thenReturn(user);

        ResponseEntity createExpense = expenseService.singleExpense(expense.getIdExpense());
    }

    @Test
    public void getLastExpense(){
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new JwtUserDetails(), null));


        when(expenseRepository.findTop1ByGroupNameAndStatusOrderByCreatedDateDesc("GroupName",true)).thenReturn(expense);
        expenseService.getLastExpense();
    }

    @Test
    public void managementExpense() throws IOException, MessagingException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new JwtUserDetails(), null));

        //set JWT user details group ?

        expense = new Expense();
        expense.setIdExpense("ID_EXPENSE");
        expense.setGroupName("Group Test");
        expenseRequest=new ExpenseRequest();
        expenseRequest.setId("ID_EXPENSE");
        expenseRequest.setStatus(true);

        when(expenseRepository.findByIdExpense(expenseRequest.getId())).thenReturn(expense);
        when(userRepository.findByRoleAndActive("SUPER_ADMIN",true)).thenReturn(Arrays.asList(user));

        ResponseEntity createExpense = expenseService.managementExpense(expenseRequest);
    }

    @Test
    public void managementExpenseNull() throws IOException, MessagingException {
        expenseRequest=new ExpenseRequest();
        expenseRequest.setId("ID_EXPENSE");
        expenseRequest.setStatus(true);

        when(userRepository.findByRoleAndActive("SUPER_ADMIN",true)).thenReturn(Arrays.asList(user));

        ResponseEntity createExpense = expenseService.managementExpense(expenseRequest);
    }
}
