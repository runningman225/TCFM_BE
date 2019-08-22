package com.future.tcfm.controller;

import com.future.tcfm.model.User;
import com.future.tcfm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.text.ParseException;

import static com.future.tcfm.config.SecurityConfig.getCurrentUser;

@CrossOrigin("**")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping
    public ResponseEntity loadAll() {
        return ResponseEntity.ok(userService.loadAll());
    }

    @GetMapping("/search")
    public Page<User> searchAllUser(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "membersOnly", required = false, defaultValue = "false") Boolean membersOnly,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) throws ParseException {
        return userService.searchBy(query, membersOnly, page, size);
    }

    @GetMapping("/email") // ini seharusnya gk usah, cukup @GetMapping aja gmn? biar jadi /api/user?email=value
    public User getUser(@RequestParam("email") String email) {
        return userService.getUser(email);
    }

    @GetMapping("/{id}") // ini seharusnya gk usah, cukup @GetMapping aja gmn? biar jadi /api/user?email=value
    public ResponseEntity getUserById(@PathVariable("id") String id) {
        return userService.getUserById(id);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity create(
            @Nullable @RequestPart("file") MultipartFile file,
            @RequestPart("user") String userJSONString
    ) throws IOException, MessagingException {
        return userService.createUserV2(userJSONString, file);
    }

    /**
     * api dibawah untuk SUPER_ADMIN yang mengupdate data user scr eksplisit
     *
     * @param id
     * @param user
     * @return
     * @throws IOException
     */
    @PutMapping(value = "/managementUser/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity update(
            @PathVariable("id") String id,
            @RequestBody User user,
            @RequestParam(value = "newGroupAdmin",defaultValue = "") String newGroupAdmin
    ) throws IOException, MessagingException {
        return userService.manageUser(id, user,newGroupAdmin);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity update(
            @PathVariable("id") String id,
            @Nullable @RequestPart("file") MultipartFile file,
            @RequestPart("user") String userJSONString
    ) throws IOException {
        return userService.updateUserV2(id, userJSONString, file);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity delete(@PathVariable("id") String id) throws MessagingException {
        return userService.deleteUser(id);
    }
}

//    @DeleteMapping
//    public ResponseEntity deleteUser(String email) {
//        return userService.deleteUser(email);
//    }
