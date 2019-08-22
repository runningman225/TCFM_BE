package com.future.tcfm.service;

import com.future.tcfm.model.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

public interface NotificationService {
    ResponseEntity setNotificationIsReadByEmail(String email);

    void createNotification(String message, String email, String groupName, String type);

    ResponseEntity findByEmail(String id);
    ResponseEntity findAll();
    ResponseEntity getGroupNotification(String groupName, Boolean bool);
    ResponseEntity updateNotificationIsRead(String id);
    ResponseEntity getPersonalNotification(String email,Boolean isRead);

    Flux<Notification> getPersonalNotificationReactive(String email);

    //    Flux<Notification> getPersonalNotificationReactive();
    Flux<List<Notification>> getPersonalNotificationReactiveV2(String email);

//    SseEmitter streamPersonalNotification(String email);

    SseEmitter streamNotification(String ref,String type);

    ResponseEntity deletePersonalNotificationByEmail(String email);
}
