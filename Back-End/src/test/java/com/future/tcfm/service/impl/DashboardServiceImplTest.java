package com.future.tcfm.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.tcfm.model.*;
import com.future.tcfm.repository.*;
import com.future.tcfm.service.ExpenseService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DashboardServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private Group group;
    private Expense expense;
    private Expense expense2;
    private User user;

    @Before
    public void init(){
        user=new User();
        User user2 = new User();
        group=new Group();
        expense = new Expense();
        expense2 = new Expense();

        user.setEmail("userTest@jyp.com");
        user.setGroupName("groupTest");
        user2.setEmail("userTest2@jyp.com");
        user2.setGroupName("groupTest");
        user.setTotalPeriodPayed(2);
        user2.setTotalPeriodPayed(4);
        user.setBalanceUsed(10000D);
        user2.setBalanceUsed(20000D);
        expense.setTitle("TestExpense");
        expense.setLastModifiedAt(1565890529084L);
        expense.setStatus(true);
        expense.setPrice(1230000D);
        expense2.setTitle("TestExpense2");
        expense2.setLastModifiedAt(1565890529084L);
        expense2.setStatus(true);
        expense2.setPrice(500000D);
        group.setRegularPayment(50000D);
        group.setGroupBalance(100000D);
        group.setBankAccountNumber("828000001");
        group.setGroupAdmin("admin@test.com");

    }

    @Test
    public void testDashboardData(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(groupRepository.findByNameAndActive(user.getGroupName(),true)).thenReturn(group);
        when(userRepository.countByGroupName(user.getGroupName())).thenReturn(2);
        when(expenseRepository.findByGroupNameLikeAndStatusOrderByCreatedDateDesc(user.getGroupName(),true)).thenReturn(Arrays.asList(expense, expense2));

        Dashboard dashboard=dashboardService.getData(user.getEmail());

        Assert.assertNotNull("Found AdminAccountNumber must not be null", dashboard.getAdminAccountNumber());
        Assert.assertNotNull("Found groupAdminName must not be null", dashboard.getAdminName());

        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }
}
