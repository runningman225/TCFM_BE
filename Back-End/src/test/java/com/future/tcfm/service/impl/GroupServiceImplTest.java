package com.future.tcfm.service.impl;

import com.future.tcfm.model.Expense;
import com.future.tcfm.model.Group;
import com.future.tcfm.model.JwtUserDetails;
import com.future.tcfm.model.User;
import com.future.tcfm.repository.ExpenseRepository;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.JwtUserDetailsRepository;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.client.ExpectedCount;
import reactor.core.publisher.Mono;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.ExpectedCount.times;

@RunWith(MockitoJUnitRunner.class)
public class GroupServiceImplTest {
    private static final String GROUP_ID = "groupId";
    private static final String USER_ID = "userId";

    @Mock
    private JwtUserDetailsRepository jwtUserDetailsRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    GroupServiceImpl groupService;

    private Group group;
    private User user;
    private JwtUserDetails jwtUserDetails;
    private Expense expense;


    @Before
    public void init(){
        group = new Group();
        group.setName("Breakthrough");
        group.setActive(true);
    }

    @Test
    public void testFindByIdNotNull() {
        when(groupRepository.save(group)).thenReturn(group);

        groupService.createGroup(group);
        group.setIdGroup("ID_Group");

        when(groupRepository.findByNameAndActive(group.getName(),true)).thenReturn(group);
        ResponseEntity foundGroup = groupService.getGroupById(group.getIdGroup());

        Assert.assertNotNull("Found group must not be null", foundGroup);
        Assert.assertEquals("Group not found!", foundGroup.getBody());
        Assert.assertEquals(HttpStatus.NOT_FOUND, foundGroup.getStatusCode());

        verify(groupRepository, Mockito.times(1)).save(group);
        verify(groupRepository, Mockito.times(1)).findByNameAndActive("Breakthrough",true);
    }


    @Test
    public void loadAll() {
        // Data preparation
        List<Group> groups = Arrays.asList(group,group,group);
        when(groupRepository.findAll()).thenReturn(groups);

        // Method call
        List<Group> groupList= groupService.loadAll();

        // Verification
        Assert.assertThat(groupList, Matchers.hasSize(3));
        verify(groupRepository, Mockito.times(1)).findAll();
        Mockito.verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void membersGroup() {
        // Data preparation
        List<User> users = Arrays.asList(user,user,user);
        when(userRepository.findByGroupNameAndActive(user.getGroupName(),true)).thenReturn(users);

        // Method call
        List<User> memberList= groupService.membersGroup(user.getGroupName());

        // Verification
        Assert.assertThat(memberList, Matchers.hasSize(3));
        verify(userRepository, Mockito.times(1)).findByGroupNameAndActive(user.getGroupName(),true);
        Mockito.verifyNoMoreInteractions(groupRepository);
    }

    @Test
    public void createGroup() {
        Group group = new Group();
        group.setName("NotSameName");

        doReturn(group).when(groupRepository).findByName(this.group.getName());
        doReturn(group).when(groupRepository).save(group);

        ResponseEntity<?> result = groupService.createGroup(group);

        verify(groupRepository,Mockito.times(1)).save(group);
        Assert.assertEquals(result.getStatusCode(), HttpStatus.OK);
    }


    @Test
    public void updateGroup() throws MessagingException {
        Group group = new Group();
        group.setName("Group Test");
        group.setIdGroup("ID_Group");
        group.setGroupAdmin("groupAdmin@test.com");
        expense = new Expense();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new JwtUserDetails(), null));

        when(groupRepository.findByIdGroup(group.getIdGroup())).thenReturn(group);
        when(groupRepository.countAllByNameAndActive(group.getName(),true)).thenReturn(0);
        when(expenseRepository.findByGroupNameLikeOrderByCreatedDateDesc(group.getName())).thenReturn(Arrays.asList(expense));

        ResponseEntity<?> result = groupService.updateGroup(group.getIdGroup(),group);

        verify(groupRepository,Mockito.times(1)).save(group);
        Assert.assertEquals(result.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void updateGroupIdNotFound() throws MessagingException {

        Group group = new Group();
        group.setName("BOOM BOOM");
        group.setIdGroup("incorrectId");

        doReturn(group).when(groupRepository).findByIdGroup(this.group.getName());
        //doReturn(group).when(groupRepository).save(group);

        ResponseEntity<?> result = groupService.updateGroup(group.getIdGroup(),group);

//        Assert.assertNull(result);
        verify(groupRepository, Mockito.times(1)).findByIdGroup(Mockito.anyString());
        Assert.assertEquals(result.getStatusCode(),HttpStatus.NOT_FOUND);
    }


}
