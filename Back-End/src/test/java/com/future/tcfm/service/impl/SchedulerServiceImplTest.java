package com.future.tcfm.service.impl;

import com.future.tcfm.model.Group;
import com.future.tcfm.model.User;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.UserRepository;
import org.awaitility.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;


import javax.mail.MessagingException;
import java.util.Arrays;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeast;
import static org.springframework.test.web.client.ExpectedCount.times;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private SchedulerServiceImpl schedulerService;

    private User user;
    private Group group;

    @Test
    public void scheduler() throws MessagingException {
        user=new User();
        user.setGroupName("Nanana");
        user.setActive(true);
        user.setPeriodeTertinggal(12);
        group=new Group();
        group.setName("Nanana");
        group.setCurrentPeriod(25);
        group.setCreatedDate(System.currentTimeMillis());

        when(userRepository.findAllByActive(true)).thenReturn(Arrays.asList(user));
        when(groupRepository.findByName(user.getGroupName())).thenReturn(group);

        schedulerService.scheduler();



    }
    @Test
    public void schedulerReminder() throws MessagingException {
        user=new User();
        user.setGroupName("Nanana");
        user.setActive(true);

        when(userRepository.findAllByActive(true)).thenReturn(Arrays.asList(user));
        schedulerService.schedulerReminder();
    }

    @Test
    public void monthlyCashStatement() throws MessagingException {
        user=new User();
        user.setGroupName("Nanana");
        user.setActive(true);
        user.setPeriodeTertinggal(15);

        when(userRepository.findAllByActive(true)).thenReturn(Arrays.asList(user));
        schedulerService.monthlyCashStatement();

    }
}