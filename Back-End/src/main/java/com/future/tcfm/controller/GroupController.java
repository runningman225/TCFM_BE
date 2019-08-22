package com.future.tcfm.controller;

import com.future.tcfm.model.Group;
import com.future.tcfm.model.User;
import com.future.tcfm.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.text.ParseException;
import java.util.List;

import static com.future.tcfm.config.SecurityConfig.getCurrentUser;

@CrossOrigin("**")
@RestController
@RequestMapping("/api/group")
public class GroupController {
    @Autowired
    GroupService groupService;

    @GetMapping
    public List<Group> loadAll (){
        return groupService.loadAll();
    }


    @GetMapping("/{groupName}/members") //body fill with group name without ""
    public ResponseEntity membersGroup(@PathVariable("groupName") String groupName){
        return new ResponseEntity<>(groupService.membersGroup(groupName), HttpStatus.OK);
    }

    @GetMapping("/members") //body fill with group name without ""
    public ResponseEntity myGroupMembers(){
        return new ResponseEntity<>(groupService.membersGroup(getCurrentUser().getGroupName()), HttpStatus.OK);
    }

    @GetMapping("/{id}") // ini seharusnya gk usah, cukup @GetMapping aja gmn? biar jadi /api/user?email=value
    public ResponseEntity getGroupById(@PathVariable("id") String id) {
        return groupService.getGroupById(id);
    }

    @PostMapping
    public ResponseEntity createGroup(@RequestBody Group group) {
        return groupService.createGroup(group);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable("id") String id, @RequestBody Group group ) throws MessagingException {
        return groupService.updateGroup(id,group);
    }
    @GetMapping("/search")
    public Page<Group> searchAllGroup(
            //            @PathVariable("groupName") String groupName,
            @RequestParam(value = "query",required = false, defaultValue = "")String query,
            @RequestParam(value = "page",required = false, defaultValue = "0")int page,
            @RequestParam(value = "size",required = false, defaultValue = "10")int size) throws ParseException {
        return groupService.searchBy(query,page,size);
    }

//    @GetMapping("/membersByEmail")
//    public Page<User> findMyGroupMembers(@RequestParam("email") String email,
//                                         @RequestParam(value = "filter",required = false, defaultValue = "name") String filter,
//                                         @RequestParam(value = "year",required = false, defaultValue = "0") int year,
//                                         @RequestParam(value = "page",required = false, defaultValue = "0") int page,
//                                         @RequestParam(value = "size",required = false, defaultValue = "10") int size) {
//        return groupService.findMembersGroupByEmail(email,filter,year,page,size);
//    }
    @DeleteMapping("/{id}")
    public ResponseEntity disbandGroup(@PathVariable("id") String id) throws MessagingException {
        return groupService.disbandGroup(id);
    }
}
