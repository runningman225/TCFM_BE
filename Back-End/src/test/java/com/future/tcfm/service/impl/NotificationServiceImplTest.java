package com.future.tcfm.service.impl;

import com.future.tcfm.model.Group;
import com.future.tcfm.model.Notification;
import com.future.tcfm.model.NotificationEvent;
import com.future.tcfm.model.User;
import com.future.tcfm.repository.NotificationRepository;
import com.future.tcfm.service.NotificationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;
    private User user;
    private NotificationEvent notificationEvent;
/*    @Override
    public ResponseEntity findAll(){
        List<Notification> notificationList = notificationRepository.findAll();
        return new ResponseEntity<>(notificationList, HttpStatus.OK);
    }*/
    @Test
    public void onNewNotification(){
    }

    @Test
    public void deletePersonalNotificationByEmail(){
        user=new User();
        user.setEmail("email@est.com");

        notificationService.deletePersonalNotificationByEmail(user.getEmail());
    }
    @Test
    public void findAll(){
        when(notificationRepository.findAll()).thenReturn(Arrays.asList(notification));

        ResponseEntity response  = notificationService.findAll();

        Assert.assertNotNull("notNULL",response);

        verify(notificationRepository, Mockito.times(1)).findAll();

    }


}
