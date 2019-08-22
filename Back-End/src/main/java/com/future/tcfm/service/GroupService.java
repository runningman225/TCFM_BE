package com.future.tcfm.service;

import com.future.tcfm.model.Group;
import com.future.tcfm.model.User;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import javax.mail.MessagingException;
import java.text.ParseException;
import java.util.List;

public interface GroupService {
    List<Group> loadAll();
    ResponseEntity createGroup(Group group);
    ResponseEntity updateGroup(String id,Group group) throws MessagingException;
    List<User> membersGroup(String groupName);
    ResponseEntity disbandGroup(String id) throws MessagingException;
    ResponseEntity getGroupById(String id);
    Page<Group> searchBy(String query, int page, int size) throws ParseException;
}
