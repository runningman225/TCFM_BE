package com.future.tcfm.service;

import com.future.tcfm.model.User;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {
    List<User> loadAll();
    ResponseEntity getUserById(String id);
    User getUser(String email);
    ResponseEntity createUserV2(String userJSONString, MultipartFile file) throws IOException, MessagingException;

    ResponseEntity manageUser(String id, User user,String newGroupAdmin) throws MessagingException;

    ResponseEntity updateUserV2(String id, String userJSONString, MultipartFile file) throws IOException;
    ResponseEntity getImage(String imageName) throws IOException;

    Page<User> searchBy(String query,Boolean membersOnly,int page, int size);

    ResponseEntity deleteUser(String email) throws MessagingException;
}
