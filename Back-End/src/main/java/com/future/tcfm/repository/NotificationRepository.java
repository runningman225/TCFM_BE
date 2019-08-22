package com.future.tcfm.repository;


import com.future.tcfm.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification,String> {
    List<Notification> findTop10ByEmailAndTypeOrderByTimestampDesc(String email,String type);
    List<Notification> findTop10ByEmailAndTypeAndIsReadOrderByTimestampDesc(String email,String type,Boolean isRead);

    List<Notification> findTop10ByGroupNameAndTypeOrderByTimestampDesc(String groupName,String type);
    void deleteAllByEmailAndType(String email,String type);
    List<Notification> findByEmailOrderByTimestampDesc(String email);
    List<Notification> findByGroupNameOrderByTimestampDesc(String groupName);
//    List<Notification> findByEmailOrderByTimestampDesc(String email, Pageable pageable);
//    List<Notification> findByGroupNameOrderByTimestampDesc(String groupName,Pageable pageable);
//    List<Notification> findByEmailOrGroupNameAndIsRead(String email,String groupName, Boolean bool);
//    List<Notification> findByEmailOrGroupName(String email,String groupName);
    List<Notification> findByEmailAndIsReadOrderByTimestampDesc(String email,Boolean bool);
    List<Notification> findByGroupNameAndIsReadOrderByTimestampDesc(String groupName,Boolean bool);
}
