package com.future.tcfm.service.impl;

import com.future.tcfm.model.NotificationEvent;
import com.future.tcfm.model.Notification;
import com.future.tcfm.model.User;
import com.future.tcfm.repository.NotificationRepository;
import com.future.tcfm.repository.UserRepository;
import com.future.tcfm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;


@Service
@EnableScheduling
public class NotificationServiceImpl implements NotificationService {
    public static final String TYPE_PERSONAL = "PERSONAL";
    public static final String TYPE_GROUP = "GROUP";
    public static final String GROUP_PROFILE_UPDATE = "Group profile had been updated by ";
    public static final String PAYMENT_LATE = " you are late for this month's payment";
    public static final String EXPENSE_MESSAGE = " requested new expense ";
    public static final String EXPENSE_APPROVED_MESSAGE = " 's requested expense had been approved ";
    public static final String EXPENSE_REJECTED_MESSAGE = " 's requested expense had been rejected ";
    public static final String USER_LEFT_GROUP = " just left this group ";
    public static final String USER_JOINED_GROUP = " just joined this group ";
    public static final String PAYMENT_MESSAGE = " had made payment ";
    public static final String PAYMENT_APPROVED_MESSAGE = " 's payment had been approved by ";
    public static final String PAYMENT_REJECTED_MESSAGE = " 's payment had been rejected by ";


    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    UserRepository userRepository;


    @Override
    public ResponseEntity findAll(){
        List<Notification> notificationList = notificationRepository.findAll();
        return new ResponseEntity<>(notificationList,HttpStatus.OK);
    }

    @Override
    public ResponseEntity getGroupNotification(String groupName, Boolean isRead) {
        List<Notification> notificationList;
        if(isRead==null){
            notificationList=notificationRepository.findByGroupNameOrderByTimestampDesc(groupName);
            return new ResponseEntity<>(notificationList,HttpStatus.OK);
        }
        notificationList= notificationRepository.findByGroupNameAndIsReadOrderByTimestampDesc(groupName,isRead);
        return new ResponseEntity<>(notificationList,HttpStatus.OK);
    }

    @Override
    public ResponseEntity getPersonalNotification(String email,Boolean isRead) {
        List<Notification> notificationList;
        if(isRead==null){
            notificationList=notificationRepository.findByEmailOrderByTimestampDesc(email);
            return new ResponseEntity<>(notificationList,HttpStatus.OK);
        }
        notificationList= notificationRepository.findByEmailAndIsReadOrderByTimestampDesc(email,isRead);
        return new ResponseEntity<>(notificationList,HttpStatus.OK);
    }

    @Override
    public ResponseEntity updateNotificationIsRead(String id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        if(notification.isPresent()) {
            notification.get().setIsRead(true);
            notification.get().setIsReadAt(System.currentTimeMillis());
            notificationRepository.save(notification.get());
            return new ResponseEntity<>(notification.get(),HttpStatus.OK);
        }
        return new ResponseEntity<>("err: Notification Not found 404",HttpStatus.NOT_FOUND);
    }



    @Override
    public ResponseEntity setNotificationIsReadByEmail(String email) {
        List<Notification> notificationList = notificationRepository.findByEmailAndIsReadOrderByTimestampDesc(email,false);
        if(notificationList == null) {
            return new ResponseEntity<>("All notifications is read", HttpStatus.NOT_FOUND);
        }
        notificationList.forEach(notif -> {
            notif.setIsRead(true);
            notif.setIsReadAt(System.currentTimeMillis());
        });
        notificationRepository.saveAll(notificationList);
        return new ResponseEntity<>("Notification Updated!",HttpStatus.OK);
    }
    @Override
    public void createNotification(String message, String email, String groupName, String type) {
        Notification notification = Notification.builder()
                .email(email)
                .message(message)
                .isRead(false)
                .timestamp(System.currentTimeMillis())
                .groupName(groupName)
                .type(type).build();
        notificationRepository.save(notification);
        NotificationEvent notificationEvent = new NotificationEvent(this,type,email,groupName);
        applicationEventPublisher.publishEvent(notificationEvent);
    }

    @Override
    public ResponseEntity findByEmail(String email) {
        List<Notification> notifications = notificationRepository.findByEmailOrderByTimestampDesc(email);
        return new ResponseEntity(notifications, HttpStatus.OK);
    }
    String email;
    private List<Notification> getNotificationByEmail(long interval){

        return notificationRepository.findByEmailOrderByTimestampDesc(email);
//        return notificationRepository.findAll();
    }

    /**
     *
     * @return notification one by one every X seconds
     */
    @Override
    public Flux<Notification> getPersonalNotificationReactive(String email){
//        this.email = getCurrentUser().getEmail();
        this.email = email;
        return Flux.interval(Duration.ofSeconds(1))
                .onBackpressureDrop()
                .map(this::getNotificationByEmail)
                .flatMapIterable(x -> x);
    }
    /**
     *
     * @Return notificationList every X secodns
     */
    List<Notification> notificationList=new ArrayList<>();
    @Override
    public Flux<List<Notification>> getPersonalNotificationReactiveV2(String email){
//        this.email = getCurrentUser().getEmail();
        this.notificationList = notificationRepository.findTop10ByEmailAndTypeOrderByTimestampDesc(email,TYPE_PERSONAL);//problem disini
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
        Flux<List<Notification>> notificationFlux = Flux.fromStream(Stream.generate(() -> this.notificationList));
        return Flux.zip(interval, notificationFlux).map(Tuple2::getT2);
    }

//    /**
//     * Stream Notifikasi yang digunakan adalah yang dibawah ini.
//     * Hanya mengirim event ketika ada perubahan pada database
//     * @param email
//     * @return
//     */
//    @Override
//    public SseEmitter streamPersonalNotification(String email) {
//        SseEmitter emitter = new SseEmitter();
//
//        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
//        this.notificationList=notificationRepository.findTop10ByEmailOrderByTimestampDesc(email);
//        sseMvcExecutor.execute(() -> {
//        SseEmitter.SseEventBuilder event = SseEmitter.event();
//            try {
//                event.id(UUID.randomUUID().toString());
//                event.name("start");
//                event.data(this.notificationList);
//                emitter.send(event);
//                System.out.println("first notification is sent");
//                for (int i = 0; true; i++) {
//                    if(this.notificationList.size()!=notificationRepository.findTop10ByEmailOrderByTimestampDesc(email).size()) {
//                        this.notificationList = notificationRepository.findTop10ByEmailOrderByTimestampDesc(email);
//                        event = SseEmitter.event()
//                                .id(String.valueOf(i+"_"+UUID.randomUUID().toString()))
//                                .name("message")
//                                .data(this.notificationList);
//                        emitter.send(event);
//                        System.out.println("new update on notification is sent");
//                    }
//                    Thread.sleep(1000);
//                }
//            } catch (Exception ex) {
//                emitter.completeWithError(ex);
//            }
//        });
//
//        return emitter;
//    }

    @Override
    public SseEmitter streamNotification(String ref,String type) {
        SseEmitter emitter = new SseEmitter(15*1000L);
        List<Notification> notificationList;
        User userExist = userRepository.findByEmailAndActive(ref,true);
        if(userExist==null){
            throw new RuntimeException("404 Error : user not found!");
        }
        if(type.equalsIgnoreCase(TYPE_GROUP)) {
           notificationList = notificationRepository.findTop10ByGroupNameAndTypeOrderByTimestampDesc(userExist.getGroupName(),TYPE_GROUP);
        }
        else{
            notificationList = notificationRepository.findTop10ByEmailAndTypeOrderByTimestampDesc(userExist.getEmail(),TYPE_PERSONAL);
        }
        this.emitters.put(ref,emitter);
        try{
            SseEmitter.SseEventBuilder event = SseEmitter.event();
            event.data(notificationList);
            event.id(UUID.randomUUID().toString());
            event.name("start");
            emitter.send(event);
        }catch (Exception ex){
            this.emitters.remove(ref);
        }
        emitter.onCompletion(() -> this.emitters.remove(ref));
        emitter.onTimeout(() -> {
            emitter.complete();
            this.emitters.remove(ref);
        });
        return emitter;
    }

    private final Map<String,SseEmitter> emitters = new ConcurrentHashMap<>();


    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Async
    @EventListener
    public void onNewNotification(NotificationEvent notificationEvent){
        List<String> deadEmitters = new ArrayList<>();
        List<Notification> notificationList;
        String eventName;
        if (notificationEvent.getType().equalsIgnoreCase(TYPE_GROUP)) {
            notificationList = notificationRepository.findTop10ByGroupNameAndTypeOrderByTimestampDesc(notificationEvent.getGroupName(),TYPE_GROUP);
            System.out.println("Event Group triggered!");
            eventName = "group";
            this.emitters.forEach((email,emitter) -> { //key = email
                executor.execute(() -> {
                    try {
                        SseEmitter.SseEventBuilder event = SseEmitter.event();
                        event.name(email + eventName);
                        event.id(UUID.randomUUID().toString());
                        event.data(notificationList);
                        event.reconnectTime(10000L);
                        emitter.send(event);
                        System.out.println(notificationEvent.getType() + " notification sent to " + email + ", eventName : " + email + eventName);
                    } catch (Exception e) {
//                    deadEmitters.add(k);
                        this.emitters.remove(email);
                        System.out.println("Exception! : removed emitter " + email);
                    }
                });
            });
        } else {
            notificationList = notificationRepository.findTop10ByEmailAndTypeOrderByTimestampDesc(notificationEvent.getEmail(),TYPE_PERSONAL);
            System.out.println("Event Personal triggered!");
            eventName = "personal";
            executor.execute(() -> {
                try {
                    SseEmitter.SseEventBuilder event = SseEmitter.event();
                    event.name(notificationEvent.getEmail() + eventName);
                    event.id(UUID.randomUUID().toString());
                    event.data(notificationList);
                    event.reconnectTime(10000L);
                    this.emitters.get(notificationEvent.getEmail()).send(event);
                } catch (Exception e) {
                    this.emitters.remove(notificationEvent.getEmail());
                    System.out.println("Exception! : removed emitter " + notificationEvent.getEmail());
                }
            });
        }
            System.out.println("Client listener Total : "+this.emitters.size());
            this.emitters.remove(deadEmitters);
    }
    @Override
    public ResponseEntity deletePersonalNotificationByEmail(String email){
        notificationRepository.deleteAllByEmailAndType(email,TYPE_PERSONAL);
        return new ResponseEntity<>("",HttpStatus.OK);
    }

}
