package com.future.tcfm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.tcfm.model.*;
import com.future.tcfm.model.ReqResModel.Overview;
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
public class OverviewServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private GroupRepository groupRepository;

    //    @Mock
    private Group group;

    @InjectMocks
    private OverviewServiceImpl overviewService;

    private Expense expense;
    private Expense expense2;
    private User user;

    @Before
    public void init(){
//        MockitoAnnotations.initMocks(this);
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
        group.setBalanceUsed(10000D);

    }

    @Test
    public void testDashboardData(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(groupRepository.findByNameAndActive(user.getGroupName(),true)).thenReturn(group);
        when(userRepository.countByGroupName(user.getGroupName())).thenReturn(2);
        when(expenseRepository.findByGroupNameLikeAndStatusOrderByCreatedDateDesc(user.getGroupName(),true)).thenReturn(Arrays.asList(expense, expense2));

        Overview overview=overviewService.getData(user.getEmail());

        Assert.assertNotNull("Found LatestExpense must not be null", overview.getLatestExpense());
        Assert.assertNotNull("Found GroupBalance must not be null", overview.getGroupBalance());
        Assert.assertNotNull("Found AveragePerExpense must not be null", overview.getAveragePerExpense());
        Assert.assertNotNull("Found PaymentPaid must not be null", overview.getPaymentPaidThisMonth());
        Assert.assertNotNull("Found PercentageTotalUsed must not be null", overview.getPercentageTotalCashUsed());
        Assert.assertNotNull("Found latestJoinDate must not be null", overview.getLatestJoinDate());
        Assert.assertNotNull("Found totalMembers must not be null", overview.getTotalMembers());
        Assert.assertNotNull("Found latestExpense must not be null", overview.getLatestExpense());
      /*  Assert.assertNotNull("Found expenses must not be null", foundExpenses);
        Assert.assertEquals("Expense must contain only two expenses", 2, foundExpenses.size());
        Assert.assertSame("Found expense must equal sample category TestExpense", expense, foundExpenses.get(0));
        Assert.assertSame("Found expense must equal sample category TestExpense2", expense2, foundExpenses.get(1));
        Assert.assertEquals("TestExpense", foundExpenses.get(0).getTitle());
        Assert.assertEquals("TestExpense2", foundExpenses.get(1).getTitle());
*/
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }
}
